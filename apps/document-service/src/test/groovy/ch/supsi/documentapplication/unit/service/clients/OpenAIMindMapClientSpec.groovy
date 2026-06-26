package ch.supsi.documentapplication.unit.service.clients

import ch.supsi.documentapplication.model.Chunk
import ch.supsi.documentapplication.model.MindMap
import ch.supsi.documentapplication.service.MindMapValidator
import ch.supsi.documentapplication.service.clients.FailureTracker
import ch.supsi.documentapplication.service.clients.mindmap.OpenAIMindMapClient
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import spock.lang.Specification
import spock.lang.Subject

class OpenAIMindMapClientSpec extends Specification {

    ChatClient chatClient = Mock()
    ChatClient.ChatClientRequestSpec requestSpec = Mock()
    ChatClient.CallResponseSpec callSpec = Mock()
    PromptTemplate promptTemplate = Mock()
    FailureTracker failureTracker = Mock()
    MindMapValidator mindMapValidator = Mock()

    @Subject
    OpenAIMindMapClient client

    static final String RENDERED_PROMPT = "rendered prompt"

    def setup() {
        client = new OpenAIMindMapClient(chatClient, failureTracker, mindMapValidator, promptTemplate)
    }

    def "should extract mind map on first successful validation"() {
        given:
        Long firstChunkId = 1L
        Long secondChunkId = 2L

        Chunk firstChunk = Mock()
        Chunk secondChunk = Mock()
        firstChunk.getId() >> firstChunkId
        secondChunk.getId() >> secondChunkId

        List<Chunk> chunks = [firstChunk, secondChunk]
        List<Long> expectedChunkIds = [firstChunkId, secondChunkId]

        MindMap mindMap = Mock()

        when:
        MindMap result = client.extractMindMap(chunks)

        then:
        1 * promptTemplate.render({ Map<String, Object> model ->
            model["chunks"] == chunks
        }) >> RENDERED_PROMPT
        1 * chatClient.prompt(_ as Prompt) >> requestSpec
        1 * requestSpec.call() >> callSpec
        1 * callSpec.entity(MindMap.class) >> mindMap
        1 * mindMapValidator.validate(mindMap, expectedChunkIds) >> true
        0 * failureTracker.registerFailure()
        1 * failureTracker.resetFailures()

        and:
        result == mindMap
    }

    def "should retry until validation succeeds and reset failures at the end"() {
        given:
        Long chunkId = 1L

        Chunk chunk = Mock()
        chunk.getId() >> chunkId

        List<Chunk> chunks = [chunk]
        List<Long> expectedChunkIds = [chunkId]

        MindMap invalidMindMap = Mock()
        MindMap validMindMap = Mock()

        when:
        MindMap result = client.extractMindMap(chunks)

        then:
        1 * promptTemplate.render({ Map<String, Object> model ->
            model["chunks"] == chunks
        }) >> RENDERED_PROMPT
        2 * chatClient.prompt(_ as Prompt) >> requestSpec
        2 * requestSpec.call() >> callSpec
        1 * callSpec.entity(MindMap.class) >> invalidMindMap
        1 * callSpec.entity(MindMap.class) >> validMindMap
        1 * mindMapValidator.validate(invalidMindMap, expectedChunkIds) >> false
        1 * mindMapValidator.validate(validMindMap, expectedChunkIds) >> true
        1 * failureTracker.registerFailure()
        1 * failureTracker.resetFailures()

        and:
        result == validMindMap
    }
}