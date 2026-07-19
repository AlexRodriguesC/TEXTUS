package ch.supsi.documentapplication.unit.service.clients

import ch.supsi.documentapplication.exception.AIOutputMappingFailedException
import ch.supsi.documentapplication.exception.RetriesFailedException
import ch.supsi.documentapplication.model.MindMap
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.service.RetryTemplateWrapper
import ch.supsi.documentapplication.service.validator.MindMapValidator
import ch.supsi.documentapplication.service.clients.OpenAIMindMapClient
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.retry.TransientAiException
import org.springframework.core.retry.RetryException
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.retry.RetryTemplate
import spock.lang.Specification
import spock.lang.Subject

import java.time.Duration

class OpenAIMindMapClientSpec extends Specification {

    @Subject
    OpenAIMindMapClient client

    ChatClient chatClient = Mock()
    ChatClient.ChatClientRequestSpec requestSpec = Mock()
    ChatClient.CallResponseSpec callResponseSpec = Mock()
    RetryTemplateWrapper retryTemplate
    MindMapValidator mindMapValidator = Mock()
    PromptTemplate promptTemplate = Mock()

    def setup() {
        def retryPolicy = RetryPolicy.builder()
                .maxRetries(2)
                .includes(TransientAiException.class)
                .delay(Duration.ofMillis(10))
                .build()
        retryTemplate = new RetryTemplateWrapper(new RetryTemplate(retryPolicy))

        client = new OpenAIMindMapClient(chatClient, retryTemplate, mindMapValidator, promptTemplate)
    }

    static Topic topic(Long id, String title, String summary, List<String> labels = List.of("abc")) {
        return new Topic(id, "a", 1, title, summary, labels)
    }

    static String randomStr() {
        return UUID.randomUUID().toString()
    }

    def "should call chatClient, validate and return the mind map"() {
        given: "a list of topics and a rendered prompt"
        def topics = [topic(1L, randomStr(), randomStr()), topic(2L, randomStr(), randomStr())]
        def expectedChunkIds = [1L, 2L]
        def expectedMindMap = new MindMap()

        promptTemplate.render(_ as Map) >> "rendered prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.entity(MindMap.class) >> expectedMindMap

        when: "getResult is invoked"
        def result = client.getResult(topics)

        then: "the validator is called with the mind map and expected chunk ids"
        1 * mindMapValidator.validate(expectedMindMap, expectedChunkIds)

        and: "the returned mind map matches what the chatClient produced"
        result == expectedMindMap
    }

    def "should retry and eventually throw RetriesFailedException when validations keep failing"() {
        given: "a list of topics and a rendered prompt"
        def topics = [topic(1L, randomStr(), randomStr())]
        def mindMap = new MindMap()

        promptTemplate.render(_ as Map) >> "rendered prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.entity(MindMap.class) >> mindMap
        mindMapValidator.validate(mindMap, [1L]) >> { throw new AIOutputMappingFailedException("invalid") }

        when: "getResult is invoked"
        client.getResult(topics)

        then: "the validation exception propagates"
        thrown(RetriesFailedException)
    }

    def "should retry and eventually throw RetriesFailedException when chatClient keeps failing transiently"() {
        given: "a list of topics"
        def topics = [topic(1L, randomStr(), randomStr())]

        promptTemplate.render(_ as Map) >> "rendered prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> { throw new TransientAiException("transient failure") }

        when: "getResult is invoked"
        client.getResult(topics)

        then: "a RetriesFailedException wrapping the transient failure is thrown"
        thrown(RetriesFailedException)
    }
}
