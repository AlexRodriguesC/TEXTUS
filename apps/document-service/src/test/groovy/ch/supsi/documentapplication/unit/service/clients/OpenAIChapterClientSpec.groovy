package ch.supsi.documentapplication.unit.service.clients

import ch.supsi.documentapplication.model.AiUsage
import ch.supsi.documentapplication.model.learn.Chapter
import ch.supsi.documentapplication.service.RetryTemplateWrapper
import ch.supsi.documentapplication.service.async.learningpipeline.Label
import ch.supsi.documentapplication.service.clients.OpenAIChapterClient
import ch.supsi.documentapplication.service.validator.ChapterValidator
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.retry.RetryTemplate
import spock.lang.Specification
import spock.lang.Subject
import tools.jackson.databind.ObjectMapper

import java.time.Duration

class OpenAIChapterClientSpec extends Specification {

    @Subject
    OpenAIChapterClient client

    ChatClient chatClient = Mock()
    ChatClient.ChatClientRequestSpec requestSpec = Mock()
    ChatClient.CallResponseSpec callResponseSpec = Mock()
    RetryTemplateWrapper retryTemplate
    PromptTemplate promptTemplate = Mock()
    PromptTemplate feedbackPromptTemplate = Mock()
    ObjectMapper objectMapper = new ObjectMapper()
    ChapterValidator chapterValidator = new ChapterValidator()

    def setup() {
        def retryPolicy = RetryPolicy.builder()
                .maxRetries(2)
                .delay(Duration.ofMillis(10))
                .build()
        retryTemplate = new RetryTemplateWrapper(new RetryTemplate(retryPolicy))

        requestSpec.options(_) >> requestSpec

        client = new OpenAIChapterClient(
                objectMapper,
                chatClient,
                retryTemplate,
                "",
                promptTemplate,
                "",
                feedbackPromptTemplate,
                chapterValidator,
                ClientTestSupport.TEST_AI_CONTEXT)
    }

    def "should return valid chapters matching all label ids when chatClient produces a complete assignment"() {
        given:
        def labels = [
                new Label(1L, "intro"),
                new Label(2L, "basics"),
                new Label(3L, "advanced")
        ]
        def expectedChapters = [
                new Chapter(0, "Foundations", "Core concepts overview", [1L, 2L, 3L])
        ]

        promptTemplate.render(_ as Map) >> "rendered prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.responseEntity(_ as ParameterizedTypeReference) >> ClientTestSupport.responseEntityFor(expectedChapters)

        when:
        def result = client.getResult(labels)

        then:
        result.result()*.index == [0]
        result.result()*.labels == [[1L, 2L, 3L]]
        result.usages() == [new AiUsage(10, 20, "test-provider", "test-model")]
        0 * feedbackPromptTemplate._
    }
}
