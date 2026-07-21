package ch.supsi.documentapplication.unit.service.clients

import ch.supsi.documentapplication.model.ClientResult
import ch.supsi.documentapplication.service.RetryTemplateWrapper
import ch.supsi.documentapplication.service.clients.Client
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.ai.retry.TransientAiException
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.retry.RetryTemplate
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClientResponseException
import spock.lang.Specification
import spock.lang.Subject

import java.time.Duration

class ModelFallbackClientSpec extends Specification {

    @Subject
    Client<String, String> client

    ChatClient chatClient = Mock()
    ChatClient.ChatClientRequestSpec requestSpec = Mock()
    ChatClient.CallResponseSpec callResponseSpec = Mock()
    PromptTemplate promptTemplate = Mock()
    BeanOutputConverter<String> beanOutputConverter = Mock()
    RetryTemplateWrapper retryTemplate

    def setup() {
        def retryPolicy = RetryPolicy.builder()
                .maxRetries(2)
                .includes(TransientAiException.class)
                .delay(Duration.ofMillis(10))
                .build()
        retryTemplate = new RetryTemplateWrapper(new RetryTemplate(retryPolicy))

        requestSpec.options(_) >> requestSpec

        client = createTestClient()
    }

    def "should return result from primary model when it succeeds"() {
        given:
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.responseEntity(String.class) >> ClientTestSupport.responseEntityFor("ok")

        when:
        def result = client.getResult("input")

        then:
        result.result() == "ok"
    }

    def "should fallback to second model when primary throws rate limit"() {
        given:
        int callCount = 0
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> {
            callCount++
            if (callCount == 1) {
                throw rateLimitException()
            }
            return callResponseSpec
        }
        callResponseSpec.responseEntity(String.class) >> ClientTestSupport.responseEntityFor("fallback-result")

        when:
        def result = client.getResult("input")

        then:
        result.result() == "fallback-result"
        callCount == 2
    }

    def "should fallback through all models and throw when all fail"() {
        given:
        int callCount = 0
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> {
            callCount++
            throw rateLimitException()
        }

        when:
        client.getResult("input")

        then:
        def thrown = thrown(Exception)
        callCount == 3
        thrown instanceof WebClientResponseException || thrown.cause instanceof WebClientResponseException
    }

    def "should reuse successful fallback model for second invoke in same getResult"() {
        given:
        int callCount = 0

        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> {
            callCount++
            if (callCount == 1) {
                throw rateLimitException()
            }
            return callResponseSpec
        }
        callResponseSpec.responseEntity(String.class) >> ClientTestSupport.responseEntityFor("ok")

        client = new Client<String, String>(
                chatClient,
                beanOutputConverter,
                retryTemplate,
                "",
                promptTemplate,
                ClientTestSupport.TEST_AI_CONTEXT) {
            @Override
            protected ClientResult<String> call(String input) {
                invoke(new Prompt(new UserMessage("first")), String.class)
                return invoke(new Prompt(new UserMessage("second")), String.class)
            }
        }

        when:
        def result = client.getResult("input")

        then:
        result.result() == "ok"
        callCount == 3
    }

    private Client<String, String> createTestClient() {
        return new Client<String, String>(
                chatClient,
                beanOutputConverter,
                retryTemplate,
                "",
                promptTemplate,
                ClientTestSupport.TEST_AI_CONTEXT) {
            @Override
            protected ClientResult<String> call(String input) {
                return invoke(new Prompt(new UserMessage("test")), String.class)
            }
        }
    }

    private static WebClientResponseException rateLimitException() {
        return WebClientResponseException.create(429, "Too Many Requests", HttpHeaders.EMPTY, null, null)
    }
}
