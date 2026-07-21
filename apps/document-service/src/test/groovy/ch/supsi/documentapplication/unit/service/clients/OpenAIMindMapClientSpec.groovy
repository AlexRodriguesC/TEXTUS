package ch.supsi.documentapplication.unit.service.clients

import ch.supsi.documentapplication.exception.AIOutputMappingFailedException
import ch.supsi.documentapplication.exception.RetriesFailedException
import ch.supsi.documentapplication.model.AiUsage
import ch.supsi.documentapplication.model.LabelMindMapAssignment
import ch.supsi.documentapplication.model.LabelMindMapChildSelection
import ch.supsi.documentapplication.model.LabelMindMapRootSelection
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.service.RetryTemplateWrapper
import ch.supsi.documentapplication.service.clients.OpenAIMindMapClient
import ch.supsi.documentapplication.service.mindmap.IncrementalLabelMindMapBuilder
import ch.supsi.documentapplication.service.mindmap.MindMapAssembler
import ch.supsi.documentapplication.service.validator.LabelMindMapValidator
import ch.supsi.documentapplication.service.validator.MindMapValidator
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

class OpenAIMindMapClientSpec extends Specification {

    @Subject
    OpenAIMindMapClient client

    ChatClient chatClient = Mock()
    ChatClient.ChatClientRequestSpec requestSpec = Mock()
    ChatClient.CallResponseSpec callResponseSpec = Mock()
    RetryTemplateWrapper retryTemplate
    PromptTemplate rootPromptTemplate = Mock()
    PromptTemplate childrenPromptTemplate = Mock()
    PromptTemplate feedbackPromptTemplate = Mock()
    ObjectMapper objectMapper = new ObjectMapper()
    MindMapAssembler mindMapAssembler = new MindMapAssembler()
    LabelMindMapValidator labelMindMapValidator = new LabelMindMapValidator()
    MindMapValidator mindMapValidator = new MindMapValidator()

    def setup() {
        def retryPolicy = RetryPolicy.builder()
                .maxRetries(2)
                .includes(TransientAiException.class)
                .delay(Duration.ofMillis(10))
                .build()
        retryTemplate = new RetryTemplateWrapper(new RetryTemplate(retryPolicy))

        requestSpec.options(_) >> requestSpec

        client = new OpenAIMindMapClient(
                objectMapper,
                chatClient,
                retryTemplate,
                "",
                rootPromptTemplate,
                "",
                childrenPromptTemplate,
                "",
                feedbackPromptTemplate,
                mindMapAssembler,
                labelMindMapValidator,
                mindMapValidator,
                new IncrementalLabelMindMapBuilder(),
                ClientTestSupport.TEST_AI_CONTEXT)
    }

    static Topic topic(Long id, List<String> labels) {
        new Topic(id, "doc.pdf", 1, "Title $id", "Summary $id", labels)
    }

    static LabelMindMapRootSelection rootSelection(Long labelId) {
        def selection = new LabelMindMapRootSelection()
        selection.labelId = labelId
        return selection
    }

    static LabelMindMapChildSelection childSelection(Long... labelIds) {
        def selection = new LabelMindMapChildSelection()
        selection.labelIds = labelIds as List
        return selection
    }

    def "should return assembled mind map when incremental steps assign all labels"() {
        given:
        def topics = [
                topic(1L, ["biology"]),
                topic(2L, ["biology", "cell"]),
                topic(3L, ["cell"])
        ]

        rootPromptTemplate.render(_ as Map) >> "root prompt"
        childrenPromptTemplate.render(_ as Map) >> "children prompt"
        chatClient.prompt(_ as Prompt) >>> [requestSpec, requestSpec]
        requestSpec.call() >>> [callResponseSpec, callResponseSpec]
        callResponseSpec.responseEntity(_) >>> [
                ClientTestSupport.responseEntityFor(rootSelection(0L)),
                ClientTestSupport.responseEntityFor(childSelection(1L))
        ]

        when:
        def result = client.getResult(topics)

        then:
        result.result().root.topicId == 1L
        result.result().allTopicIds == [1L, 2L, 3L] as Set
        result.usages().size() == 2
        result.usageOperations() == ["mind_map_generate", "mind_map_generate"]
        0 * feedbackPromptTemplate._
    }

    def "should call feedback loop and merge missing labels"() {
        given:
        def topics = [
                topic(1L, ["biology"]),
                topic(2L, ["cell"])
        ]

        rootPromptTemplate.render(_) >> "root prompt"
        childrenPromptTemplate.render(_) >> "children prompt"
        chatClient.prompt(_ as Prompt) >>> [requestSpec, requestSpec, requestSpec]
        requestSpec.call() >>> [callResponseSpec, callResponseSpec, callResponseSpec]
        callResponseSpec.responseEntity(_) >>> [
                ClientTestSupport.responseEntityFor(rootSelection(0L)),
                ClientTestSupport.responseEntityFor(childSelection()),
                ClientTestSupport.responseEntityFor([new LabelMindMapAssignment(0, [1L])])
        ]

        when:
        def result = client.getResult(topics)

        then:
        1 * feedbackPromptTemplate.render(_) >> "feedback prompt"
        result.result().allTopicIds == [1L, 2L] as Set
        result.usages().size() == 3
        result.usageOperations() == ["mind_map_generate", "mind_map_generate", "mind_map_feedback"]
    }

    def "should sanitize duplicate labels without feedback"() {
        given:
        def topics = [
                topic(1L, ["biology"]),
                topic(2L, ["cell"])
        ]

        rootPromptTemplate.render(_ as Map) >> "root prompt"
        childrenPromptTemplate.render(_ as Map) >> "children prompt"
        chatClient.prompt(_ as Prompt) >>> [requestSpec, requestSpec]
        requestSpec.call() >>> [callResponseSpec, callResponseSpec]
        callResponseSpec.responseEntity(_) >>> [
                ClientTestSupport.responseEntityFor(rootSelection(0L)),
                ClientTestSupport.responseEntityFor(childSelection(0L, 1L))
        ]

        when:
        def result = client.getResult(topics)

        then:
        result.result().allTopicIds == [1L, 2L] as Set
        0 * feedbackPromptTemplate._
    }

    def "should retry and eventually throw RetriesFailedException when validations keep failing"() {
        given:
        def topics = [
                topic(1L, ["biology"]),
                topic(2L, ["cell"])
        ]

        rootPromptTemplate.render(_ as Map) >> "root prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.responseEntity(LabelMindMapRootSelection.class) >> ClientTestSupport.responseEntityFor(new LabelMindMapRootSelection())

        when:
        client.getResult(topics)

        then:
        thrown(RetriesFailedException)
    }

    def "should retry and eventually throw RetriesFailedException when chatClient keeps failing transiently"() {
        given:
        def topics = [
                topic(1L, ["biology"]),
                topic(2L, ["cell"])
        ]

        rootPromptTemplate.render(_ as Map) >> "root prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> { throw new TransientAiException("transient failure") }

        when:
        client.getResult(topics)

        then:
        thrown(RetriesFailedException)
    }
}
