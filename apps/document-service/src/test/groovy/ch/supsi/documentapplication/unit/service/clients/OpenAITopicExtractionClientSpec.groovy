package ch.supsi.documentapplication.unit.service.clients

import ch.supsi.documentapplication.model.Page
import ch.supsi.documentapplication.model.Topic
import ch.supsi.documentapplication.service.clients.OpenAITopicExtractionClient
import org.apache.commons.lang3.tuple.Pair
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.retry.TransientAiException
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.retry.RetryException
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.retry.RetryTemplate
import spock.lang.Specification
import spock.lang.Subject

import java.time.Duration

class OpenAITopicExtractionClientSpec extends Specification {

    @Subject
    OpenAITopicExtractionClient client

    ChatClient chatClient = Mock()
    ChatClient.ChatClientRequestSpec requestSpec = Mock()
    ChatClient.CallResponseSpec callResponseSpec = Mock()
    RetryTemplate retryTemplate
    PromptTemplate promptTemplate = Mock()

    def setup() {
        def retryPolicy = RetryPolicy.builder()
                .maxRetries(2)
                .includes(TransientAiException.class)
                .delay(Duration.ofMillis(10))
                .build()
        retryTemplate = new RetryTemplate(retryPolicy)

        client = new OpenAITopicExtractionClient(chatClient, retryTemplate, promptTemplate)
    }

    Topic topic(Long id, String title, String summary, List<String> labels = List.of("abc")) {
        return new Topic(id, title, summary, labels)
    }

    def "should extract distinct labels from existing topics, build prompt and return extracted topics"() {
        given: "a page and existing topics with overlapping labels"
        def page = new Page(1L, "title1", "page summary text")
        def existingTopics = [
                topic(1L, "topicA", "summ", ["label1", "label2"]),
                topic(2L, "topicB", "summ", ["label2", "label3"])
        ]
        def input = Pair.of(page, existingTopics)
        def expectedTopics = [topic(3L, "newTopic", "summ", ["label4"])]

        promptTemplate.render(_ as Map) >> "rendered prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.entity(_ as ParameterizedTypeReference) >> expectedTopics

        when: "getResult is invoked"
        def result = client.getResult(input)

        then: "the prompt is rendered using the page summary and distinct labels"
        1 * promptTemplate.render({ Map params ->
            params["page_text"] == "page summary text" &&
                    params["existing_labels"] as Set == ["label1", "label2", "label3"] as Set
        }) >> "rendered prompt"

        and: "the returned topics match what the chatClient produced"
        result == expectedTopics
    }

    def "should retry and eventually throw RetryException when chatClient keeps failing transiently"() {
        given: "a page and empty existing topics"
        def page = new Page(1L, "title1", "page summary text")
        def input = Pair.of(page, [])

        promptTemplate.render(_ as Map) >> "rendered prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> { throw new TransientAiException("transient failure") }

        when: "getResult is invoked"
        client.getResult(input)

        then: "a RetryException wrapping the transient failure is thrown"
        def e = thrown(RetryException)
        e.cause instanceof TransientAiException
    }

    def "should handle empty existing topics without labels"() {
        given: "a page with no existing topics"
        def page = new Page(1L, "title1", "page summary text")
        def input = Pair.of(page, [])
        def expectedTopics = [topic(1L, "topic1", "summ", ["label1"])]

        promptTemplate.render(_ as Map) >> "rendered prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.entity(_ as ParameterizedTypeReference) >> expectedTopics

        when: "getResult is invoked"
        def result = client.getResult(input)

        then: "the prompt is rendered with an empty labels list"
        1 * promptTemplate.render({ Map params -> params["existing_labels"] == [] }) >> "rendered prompt"

        and: "the extracted topics are returned"
        result == expectedTopics
    }
}
