package ch.supsi.documentapplication.unit.service.clients

import ch.supsi.documentapplication.exception.AIOutputMappingFailedException
import ch.supsi.documentapplication.exception.RetriesFailedException
import ch.supsi.documentapplication.model.ClientResult
import ch.supsi.documentapplication.service.RetryTemplateWrapper
import ch.supsi.documentapplication.service.clients.Client
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.ai.openai.OpenAiChatOptions
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
    List<String> requestedModels = []

    def setup() {
        def retryPolicy = RetryPolicy.builder()
                .maxRetries(2)
                .includes(TransientAiException.class)
                .delay(Duration.ofMillis(10))
                .build()
        retryTemplate = new RetryTemplateWrapper(new RetryTemplate(retryPolicy))

        requestSpec.options(_ as OpenAiChatOptions.Builder) >> { OpenAiChatOptions.Builder builder ->
            requestedModels << builder.build().model
            requestSpec
        }

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
        requestedModels == ["test-model"]
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
        requestedModels == ["test-model", "fallback-model-2"]
    }

    def "should fallback through all models then outer-retry from model 1 and succeed"() {
        given:
        int callCount = 0
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> {
            callCount++
            if (callCount <= 3) {
                throw rateLimitException()
            }
            return callResponseSpec
        }
        callResponseSpec.responseEntity(String.class) >> ClientTestSupport.responseEntityFor("ok-after-retry")

        when:
        def result = client.getResult("input")

        then:
        result.result() == "ok-after-retry"
        callCount == 4
        requestedModels == [
                "test-model", "fallback-model-2", "fallback-model-3",
                "test-model"
        ]
    }

    def "should exhaust all models on every outer attempt then throw RetriesFailedException"() {
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
        def thrown = thrown(RetriesFailedException)
        callCount == 9
        requestedModels == [
                "test-model", "fallback-model-2", "fallback-model-3",
                "test-model", "fallback-model-2", "fallback-model-3",
                "test-model", "fallback-model-2", "fallback-model-3"
        ]
        thrown.cause instanceof WebClientResponseException
                || thrown.cause?.cause instanceof WebClientResponseException
    }

    def "should reuse successful fallback model for second invoke in same operation"() {
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
        requestedModels == ["test-model", "fallback-model-2", "fallback-model-2"]
    }

    def "should restart whole operation on next model when validation fails after first invoke"() {
        given:
        int callCount = 0
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> {
            callCount++
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
                def first = invoke(new Prompt(new UserMessage("first")), String.class)
                if ("test-model".equals(requestedModels.last())) {
                    throw AIOutputMappingFailedException.create("test", "invalid on primary")
                }
                return first
            }
        }

        when:
        def result = client.getResult("input")

        then:
        result.result() == "ok"
        callCount == 2
        requestedModels == ["test-model", "fallback-model-2"]
    }

    def "should fallback on transient AI failures within the same outer attempt"() {
        given:
        int callCount = 0
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> {
            callCount++
            if (callCount == 1) {
                throw new TransientAiException("temporary glitch")
            }
            return callResponseSpec
        }
        callResponseSpec.responseEntity(String.class) >> ClientTestSupport.responseEntityFor("ok")

        when:
        def result = client.getResult("input")

        then:
        result.result() == "ok"
        callCount == 2
        requestedModels == ["test-model", "fallback-model-2"]
    }

    def "should fail fast without model fallback for non-retryable errors"() {
        given:
        int callCount = 0
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> {
            callCount++
            throw new IllegalStateException("misconfiguration")
        }

        when:
        client.getResult("input")

        then:
        thrown(IllegalStateException)
        callCount == 1
        requestedModels == ["test-model"]
    }

    def "should reset to primary model for a subsequent getResult call"() {
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

        when:
        def first = client.getResult("input")
        def second = client.getResult("input")

        then:
        first.result() == "ok"
        second.result() == "ok"
        requestedModels == ["test-model", "fallback-model-2", "test-model"]
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
