package ch.supsi.documentapplication.unit.service.clients

import ch.supsi.documentapplication.exception.RetriesFailedException
import ch.supsi.documentapplication.model.AiUsage
import ch.supsi.documentapplication.service.RetryTemplateWrapper
import ch.supsi.documentapplication.service.async.learningpipeline.ChapterBoundary
import ch.supsi.documentapplication.service.async.learningpipeline.ChapterGenerationRequest
import ch.supsi.documentapplication.service.async.learningpipeline.ChapterTopic
import ch.supsi.documentapplication.service.clients.OpenAIChapterClient
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.retry.TransientAiException
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
    ObjectMapper objectMapper = new ObjectMapper()
    Map capturedPromptParams

    def setup() {
        def retryPolicy = RetryPolicy.builder()
                .maxRetries(2)
                .includes(TransientAiException.class)
                .delay(Duration.ofMillis(10))
                .build()
        retryTemplate = new RetryTemplateWrapper(new RetryTemplate(retryPolicy))

        requestSpec.options(_) >> requestSpec
        capturedPromptParams = null
        promptTemplate.render(_ as Map) >> { Map params ->
            capturedPromptParams = new HashMap(params)
            "rendered prompt"
        }

        client = new OpenAIChapterClient(
                objectMapper,
                chatClient,
                retryTemplate,
                "",
                promptTemplate,
                ClientTestSupport.TEST_AI_CONTEXT)
    }

    def "should return a validated chapter boundary and pass topics into the prompt"() {
        given:
        def topics = [
                new ChapterTopic(1, "home cooking chicken"),
                new ChapterTopic(2, "kitchen safety basics"),
                new ChapterTopic(3, "advanced plating techniques")
        ]
        def request = new ChapterGenerationRequest(topics)
        def expected = new ChapterBoundary("Home Cooking", "Covers home cooking foundations", 2)

        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.responseEntity(ChapterBoundary.class) >> ClientTestSupport.responseEntityFor(expected)

        when:
        def result = client.getResult(request)

        then:
        result.result().title == "Home Cooking"
        result.result().description == "Covers home cooking foundations"
        result.result().cuttingPoint == 2
        result.usages() == [new AiUsage(10, 20, "test-provider", "test-model")]
        capturedPromptParams.topics == objectMapper.writeValueAsString(topics)
    }

    def "should retry once for invalid cutting points and succeed on the second attempt"(Integer invalidCut) {
        given:
        def topics = (1..12).collect { int i -> new ChapterTopic(i, "label$i") }
        def request = new ChapterGenerationRequest(topics)
        def invalid = new ChapterBoundary("Bad Cut", "Invalid cutting point description", invalidCut)
        def valid = new ChapterBoundary("Good Cut", "Valid cutting point description", 12)
        int attempt = 0

        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> {
            attempt++
            return callResponseSpec
        }
        callResponseSpec.responseEntity(ChapterBoundary.class) >> {
            ClientTestSupport.responseEntityFor(attempt == 1 ? invalid : valid)
        }

        when:
        def result = client.getResult(request)

        then:
        result.result().cuttingPoint == 12
        attempt == 2

        where:
        invalidCut << [null, 0, 13]
    }

    def "should fail after retries are exhausted when cutting point stays invalid"() {
        given:
        def topics = (1..5).collect { int i -> new ChapterTopic(i, "label$i") }
        def request = new ChapterGenerationRequest(topics)
        def invalid = new ChapterBoundary("Bad Cut", "Invalid cutting point description", 99)
        int attempt = 0

        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> {
            attempt++
            return callResponseSpec
        }
        callResponseSpec.responseEntity(ChapterBoundary.class) >> ClientTestSupport.responseEntityFor(invalid)

        when:
        client.getResult(request)

        then:
        thrown(RetriesFailedException)
        attempt == 3
    }

    def "should fail validation when title or description is blank"() {
        given:
        def topics = [new ChapterTopic(1, "solo topic")]
        def request = new ChapterGenerationRequest(topics)
        def invalid = new ChapterBoundary("", "Missing title description text", 1)
        int attempt = 0

        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> {
            attempt++
            return callResponseSpec
        }
        callResponseSpec.responseEntity(ChapterBoundary.class) >> ClientTestSupport.responseEntityFor(invalid)

        when:
        client.getResult(request)

        then:
        thrown(RetriesFailedException)
        attempt == 3
    }
}
