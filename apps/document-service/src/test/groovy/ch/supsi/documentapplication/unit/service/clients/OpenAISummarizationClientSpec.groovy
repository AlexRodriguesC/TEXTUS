package ch.supsi.documentapplication.unit.service.clients

import ch.supsi.documentapplication.exception.RetriesFailedException
import ch.supsi.documentapplication.model.AiUsage
import ch.supsi.documentapplication.model.Page
import ch.supsi.documentapplication.service.RetryTemplateWrapper
import ch.supsi.documentapplication.service.clients.OpenAISummarizationClient
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.retry.TransientAiException
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.retry.RetryTemplate
import spock.lang.Specification
import spock.lang.Subject

import java.time.Duration

class OpenAISummarizationClientSpec extends Specification {

    @Subject
    OpenAISummarizationClient client

    ChatClient chatClient = Mock()
    ChatClient.ChatClientRequestSpec requestSpec = Mock()
    ChatClient.CallResponseSpec callResponseSpec = Mock()
    RetryTemplateWrapper retryTemplate
    PromptTemplate promptTemplate = Mock()

    def setup() {
        def retryPolicy = RetryPolicy.builder()
                .maxRetries(2)
                .includes(TransientAiException.class)
                .delay(Duration.ofMillis(10))
                .build()
        retryTemplate = new RetryTemplateWrapper(new RetryTemplate(retryPolicy))

        client = new OpenAISummarizationClient(
                chatClient,
                retryTemplate,
                "",
                promptTemplate,
                ClientTestSupport.TEST_AI_CONTEXT)
    }

    def "should call chatClient and return the summarized page"() {
        given: "a document text and a rendered prompt"
        def documentText = "some long document content"
        def expectedPage = new Page(null, "title1", "summary1")

        promptTemplate.render(_ as Map) >> "rendered prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.responseEntity(Page.class) >> ClientTestSupport.responseEntityFor(expectedPage)

        when: "getResult is invoked"
        def result = client.getResult(documentText)

        then: "the returned page matches what the chatClient produced"
        result.result() == expectedPage
        result.usages() == [new AiUsage(10, 20, "test-provider", "test-model")]
    }

    def "should retry and eventually throw RetriesFailedException when chatClient keeps failing transiently"() {
        given: "a document text"
        def documentText = "some document content"

        promptTemplate.render(_ as Map) >> "rendered prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> { throw new TransientAiException("transient failure") }

        when: "getResult is invoked"
        client.getResult(documentText)

        then: "a RetriesFailedException wrapping the transient failure is thrown"
        thrown(RetriesFailedException)
    }

    def "should succeed after a transient failure is retried"() {
        given: "a document text and an eventual successful response"
        def documentText = "some document content"
        def expectedPage = new Page(null, "title2", "summary2")
        int attempt = 0

        promptTemplate.render(_ as Map) >> "rendered prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> {
            attempt++
            if (attempt == 1) {
                throw new TransientAiException("temporary glitch")
            }
            return callResponseSpec
        }
        callResponseSpec.responseEntity(Page.class) >> ClientTestSupport.responseEntityFor(expectedPage)

        when: "getResult is invoked"
        def result = client.getResult(documentText)

        then: "the retry succeeds and the page is returned"
        result.result() == expectedPage
        attempt == 2
    }
}
