package ch.supsi.documentapplication.unit.service.clients

import ch.supsi.documentapplication.exception.RetriesFailedException
import ch.supsi.documentapplication.model.AiUsage
import ch.supsi.documentapplication.model.Page
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.service.RetryTemplateWrapper
import ch.supsi.documentapplication.service.clients.EvaluationRestClient
import ch.supsi.documentapplication.service.clients.OpenAITopicExtractionClient
import org.apache.commons.lang3.tuple.Pair
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.retry.TransientAiException
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.retry.RetryTemplate
import spock.lang.Specification
import spock.lang.Subject
import tools.jackson.databind.ObjectMapper

import java.time.Duration

class OpenAITopicExtractionClientSpec extends Specification {

    @Subject
    OpenAITopicExtractionClient client

    ChatClient chatClient = Mock()
    ChatClient.ChatClientRequestSpec requestSpec = Mock()
    ChatClient.CallResponseSpec callResponseSpec = Mock()
    RetryTemplateWrapper retryTemplate
    PromptTemplate promptTemplate = Mock()
    EvaluationRestClient evaluationRestClient = Mock()

    def setup() {
        def retryPolicy = RetryPolicy.builder()
                .maxRetries(2)
                .includes(TransientAiException.class)
                .delay(Duration.ofMillis(10))
                .build()
        retryTemplate = new RetryTemplateWrapper(new RetryTemplate(retryPolicy))

        requestSpec.options(_) >> requestSpec

        client = new OpenAITopicExtractionClient(
                new ObjectMapper(),
                evaluationRestClient,
                chatClient,
                retryTemplate,
                "",
                promptTemplate,
                ClientTestSupport.TEST_AI_CONTEXT)
    }

    static Topic topic(Long id, String title, String summary, List<String> labels = List.of("abc")) {
        return new Topic(id, "1", 1, title, summary, labels)
    }

    def "should extract distinct labels from existing topics, build prompt and return extracted topics"() {
        given: "a page and existing topics with overlapping labels"
        def page = new Page(1L, "title1", "page summary text")
        def existingTopics = [
                topic(1L, "topicA",
                        "summ", ["label1", "label2"]),
                topic(2L, "topicB", "summ", ["label2", "label3"])
        ]
        def input = Pair.of(page, existingTopics)
        def expectedTopics = [topic(3L, "newTopic", "summ", ["label4"])]

        promptTemplate.render(_ as Map) >> "rendered prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.responseEntity(_ as ParameterizedTypeReference) >> ClientTestSupport.responseEntityFor(expectedTopics)

        when: "getResult is invoked"
        def result = client.getResult(input)

        then: "the prompt is rendered using the page summary and distinct labels"
        1 * promptTemplate.render({ Map params ->
            params["page_text"] == "page summary text" &&
                    params["existing_labels"] == '["label1","label2","label3"]'
        }) >> "rendered prompt"

        and: "the returned topics match what the chatClient produced"
        result.result() == expectedTopics
        result.usages() == [new AiUsage(10, 20, "test-provider", "test-model")]

        and: "evaluations are submitted for the topic list and each topic"
        1 * evaluationRestClient.evaluateTopicList("page summary text", expectedTopics)
        1 * evaluationRestClient.evaluateTopicHallucination("page summary text", expectedTopics[0])
    }

    def "should retry and eventually throw RetriesFailedException when chatClient keeps failing transiently"() {
        given: "a page and empty existing topics"
        def page = new Page(1L, "title1", "page summary text")
        def input = Pair.of(page, [])

        promptTemplate.render(_ as Map) >> "rendered prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> { throw new TransientAiException("transient failure") }

        when: "getResult is invoked"
        client.getResult(input)

        then: "a RetriesFailedException wrapping the transient failure is thrown"
        thrown(RetriesFailedException)
    }

    def "should handle empty existing topics without labels"() {
        given: "a page with no existing topics"
        def page = new Page(1L, "title1", "page summary text")
        def input = Pair.of(page, [])
        def expectedTopics = [topic(1L, "topic1", "summ", ["label1"])]

        promptTemplate.render(_ as Map) >> "rendered prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.responseEntity(_ as ParameterizedTypeReference) >> ClientTestSupport.responseEntityFor(expectedTopics)

        when: "getResult is invoked"
        def result = client.getResult(input)

        then: "the prompt is rendered with an empty labels list"
        1 * promptTemplate.render({ Map params -> params["existing_labels"] == '[]' }) >> "rendered prompt"

        and: "the extracted topics are returned"
        result.result() == expectedTopics
    }
}
