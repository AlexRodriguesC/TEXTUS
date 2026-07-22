package ch.supsi.documentapplication.unit.service.clients

import ch.supsi.documentapplication.exception.RetriesFailedException
import ch.supsi.documentapplication.model.MindMapGenerationInput
import ch.supsi.documentapplication.model.Page
import ch.supsi.documentapplication.model.PageMindMapAssignment
import ch.supsi.documentapplication.model.PageMindMapChildSelection
import ch.supsi.documentapplication.model.PageMindMapRootSelection
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.service.RetryTemplateWrapper
import ch.supsi.documentapplication.service.clients.OpenAIMindMapClient
import ch.supsi.documentapplication.service.mindmap.IncrementalPageMindMapBuilder
import ch.supsi.documentapplication.service.mindmap.MindMapAssembler
import ch.supsi.documentapplication.service.validator.MindMapValidator
import ch.supsi.documentapplication.service.validator.PageMindMapValidator
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
    PageMindMapValidator pageMindMapValidator = new PageMindMapValidator()
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
                pageMindMapValidator,
                mindMapValidator,
                new IncrementalPageMindMapBuilder(),
                ClientTestSupport.TEST_AI_CONTEXT)
    }

    static Page page(Long id) {
        new Page(id, "Page $id", "Summary $id")
    }

    static Topic topic(Long id, Long pageId) {
        new Topic(id, "doc.pdf", pageId, "Title $id", "Summary $id", ["label"])
    }

    static PageMindMapRootSelection rootSelection(Long pageId) {
        def selection = new PageMindMapRootSelection()
        selection.pageId = pageId
        return selection
    }

    static PageMindMapChildSelection childSelection(Long... pageIds) {
        def selection = new PageMindMapChildSelection()
        selection.pageIds = pageIds as List
        return selection
    }

    def "should return assembled mind map when incremental steps assign all pages"() {
        given:
        def pages = [page(10L), page(20L), page(30L)]
        def topics = [topic(1L, 10L), topic(2L, 20L), topic(3L, 30L)]
        def input = new MindMapGenerationInput(pages, topics)

        rootPromptTemplate.render(_ as Map) >> "root prompt"
        childrenPromptTemplate.render(_ as Map) >> "children prompt"
        chatClient.prompt(_ as Prompt) >>> [requestSpec, requestSpec, requestSpec]
        requestSpec.call() >>> [callResponseSpec, callResponseSpec, callResponseSpec]
        callResponseSpec.responseEntity(_) >>> [
                ClientTestSupport.responseEntityFor(rootSelection(10L)),
                ClientTestSupport.responseEntityFor(childSelection(20L)),
                ClientTestSupport.responseEntityFor(childSelection(30L))
        ]

        when:
        def result = client.getResult(input)

        then:
        result.result().root.pageId == 10L
        result.result().allTopicIds == [1L, 2L, 3L] as Set
        result.result().allPageIds == [10L, 20L, 30L] as Set
        result.usages().size() == 3
        result.usageOperations() == ["mind_map_generate", "mind_map_generate", "mind_map_generate"]
        0 * feedbackPromptTemplate._
    }

    def "should call feedback loop and merge missing pages"() {
        given:
        def pages = [page(10L), page(20L)]
        def topics = [topic(1L, 10L), topic(2L, 20L)]
        def input = new MindMapGenerationInput(pages, topics)

        rootPromptTemplate.render(_) >> "root prompt"
        childrenPromptTemplate.render(_) >> "children prompt"
        chatClient.prompt(_ as Prompt) >>> [requestSpec, requestSpec, requestSpec]
        requestSpec.call() >>> [callResponseSpec, callResponseSpec, callResponseSpec]
        callResponseSpec.responseEntity(_) >>> [
                ClientTestSupport.responseEntityFor(rootSelection(10L)),
                ClientTestSupport.responseEntityFor(childSelection()),
                ClientTestSupport.responseEntityFor([new PageMindMapAssignment(0, [20L])])
        ]

        when:
        def result = client.getResult(input)

        then:
        1 * feedbackPromptTemplate.render(_) >> "feedback prompt"
        result.result().allTopicIds == [1L, 2L] as Set
        result.usages().size() == 3
        result.usageOperations() == ["mind_map_generate", "mind_map_generate", "mind_map_feedback"]
    }

    def "should sanitize duplicate pages without feedback"() {
        given:
        def pages = [page(10L), page(20L)]
        def topics = [topic(1L, 10L), topic(2L, 20L)]
        def input = new MindMapGenerationInput(pages, topics)

        rootPromptTemplate.render(_ as Map) >> "root prompt"
        childrenPromptTemplate.render(_ as Map) >> "children prompt"
        chatClient.prompt(_ as Prompt) >>> [requestSpec, requestSpec]
        requestSpec.call() >>> [callResponseSpec, callResponseSpec]
        callResponseSpec.responseEntity(_) >>> [
                ClientTestSupport.responseEntityFor(rootSelection(10L)),
                ClientTestSupport.responseEntityFor(childSelection(10L, 20L))
        ]

        when:
        def result = client.getResult(input)

        then:
        result.result().allTopicIds == [1L, 2L] as Set
        0 * feedbackPromptTemplate._
    }

    def "should retry and eventually throw RetriesFailedException when validations keep failing"() {
        given:
        def pages = [page(10L), page(20L)]
        def topics = [topic(1L, 10L), topic(2L, 20L)]
        def input = new MindMapGenerationInput(pages, topics)

        rootPromptTemplate.render(_ as Map) >> "root prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.responseEntity(PageMindMapRootSelection.class) >> ClientTestSupport.responseEntityFor(new PageMindMapRootSelection())

        when:
        client.getResult(input)

        then:
        thrown(RetriesFailedException)
    }

    def "should retry and eventually throw RetriesFailedException when chatClient keeps failing transiently"() {
        given:
        def pages = [page(10L), page(20L)]
        def topics = [topic(1L, 10L), topic(2L, 20L)]
        def input = new MindMapGenerationInput(pages, topics)

        rootPromptTemplate.render(_ as Map) >> "root prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> { throw new TransientAiException("transient failure") }

        when:
        client.getResult(input)

        then:
        thrown(RetriesFailedException)
    }
}
