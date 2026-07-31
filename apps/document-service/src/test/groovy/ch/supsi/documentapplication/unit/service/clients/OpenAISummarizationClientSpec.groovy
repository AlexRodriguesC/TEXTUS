package ch.supsi.documentapplication.unit.service.clients

import ch.supsi.documentapplication.exception.RetriesFailedException
import ch.supsi.documentapplication.model.AiUsage
import ch.supsi.documentapplication.model.PageSummarization
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

        requestSpec.options(_) >> requestSpec

        client = new OpenAISummarizationClient(
                chatClient,
                retryTemplate,
                "",
                promptTemplate,
                ClientTestSupport.TEST_AI_CONTEXT)
    }

    def "should call chatClient and return the summarized content page"() {
        given: "a document text and a rendered prompt"
        def documentText = "some long document content"
        def expectedSummarization = new PageSummarization(true, "title1", "summary1")

        promptTemplate.render(_ as Map) >> "rendered prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.responseEntity(PageSummarization.class) >> ClientTestSupport.responseEntityFor(expectedSummarization)

        when: "getResult is invoked"
        def result = client.getResult(documentText)

        then: "the returned summarization matches what the chatClient produced"
        result.result() == expectedSummarization
        result.usages() == [new AiUsage(10, 20, "test-provider", "test-model")]
    }

    def "should return rejected non-content page without validation error"() {
        given:
        def documentText = "Table of Contents\nChapter 1 .... 3"
        def rejectedSummarization = new PageSummarization(false, "", "")

        promptTemplate.render(_ as Map) >> "rendered prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.responseEntity(PageSummarization.class) >> ClientTestSupport.responseEntityFor(rejectedSummarization)

        when:
        def result = client.getResult(documentText)

        then:
        !result.result().contentPage
        result.result().title == ""
        result.result().summary == ""
    }

    def "should retry when content page is missing title or summary"() {
        given:
        def documentText = "some document content"
        def invalidSummarization = new PageSummarization(true, "", "summary only")
        int attempt = 0

        promptTemplate.render(_ as Map) >> "rendered prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> {
            attempt++
            return callResponseSpec
        }
        callResponseSpec.responseEntity(PageSummarization.class) >> ClientTestSupport.responseEntityFor(invalidSummarization)

        when:
        client.getResult(documentText)

        then:
        thrown(RetriesFailedException)
        attempt == 3
    }

    def "should wrap mapping validation failures in RetriesFailedException after retries are exhausted"() {
        given:
        def documentText = "some document content"
        def invalidSummarization = new PageSummarization(true, "title only", "   ")

        promptTemplate.render(_ as Map) >> "rendered prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.responseEntity(PageSummarization.class) >> ClientTestSupport.responseEntityFor(invalidSummarization)

        when:
        client.getResult(documentText)

        then:
        thrown(RetriesFailedException)
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
        def expectedSummarization = new PageSummarization(true, "title2", "summary2")
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
        callResponseSpec.responseEntity(PageSummarization.class) >> ClientTestSupport.responseEntityFor(expectedSummarization)

        when: "getResult is invoked"
        def result = client.getResult(documentText)

        then: "the retry succeeds and the summarization is returned"
        result.result() == expectedSummarization
        attempt == 2
    }
}
