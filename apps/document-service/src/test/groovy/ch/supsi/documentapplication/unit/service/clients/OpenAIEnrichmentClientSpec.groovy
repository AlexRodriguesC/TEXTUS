package ch.supsi.documentapplication.unit.service.clients

import ch.supsi.documentapplication.model.Chunk
import ch.supsi.documentapplication.service.clients.FailureTracker
import ch.supsi.documentapplication.service.clients.LLMInputSanitizer
import ch.supsi.documentapplication.service.clients.enrichment.OpenAIEnrichmentClient
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.metadata.ChatResponseMetadata
import org.springframework.ai.chat.metadata.Usage
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.Generation
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import spock.lang.Specification
import spock.lang.Subject

class OpenAIEnrichmentClientSpec extends Specification {

    ChatClient chatClient = Mock()
    ChatClient.ChatClientRequestSpec requestSpec = Mock()
    ChatClient.CallResponseSpec callSpec = Mock()
    PromptTemplate promptTemplate = Mock()
    ObjectMapper chunkDeserializer = Mock()
    FailureTracker failureTracker = Mock()

    @Subject
    OpenAIEnrichmentClient client

    static final String RENDERED_PROMPT = "rendered prompt"
    static final String RAW_JSON = '{"key":"rasterization"}'
    static final String TO_ENRICH = "Rasterization converts 3D primitives to 2D pixels."
    static final List<String> ROLLING_KEYS = ["pipeline", "shading"]

    def setup() {
        client = new OpenAIEnrichmentClient(chatClient, failureTracker, chunkDeserializer, new LLMInputSanitizer(), promptTemplate)
        promptTemplate.render(_ as Map) >> RENDERED_PROMPT
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> callSpec
    }

    private ChatResponse buildChatResponse(String text, int tokensIn = 10, int tokensOut = 5) {
        def message = Mock(AssistantMessage) { getText() >> text }
        def generation = Mock(Generation) { getOutput() >> message }
        def usage = Mock(Usage) {
            getPromptTokens() >> tokensIn
            getCompletionTokens() >> tokensOut
        }
        def metadata = Mock(ChatResponseMetadata) { getUsage() >> usage }
        Mock(ChatResponse) {
            getResult() >> generation
            getMetadata() >> metadata
        }
    }

    private static Chunk baseChunk() {
        def chunk = new Chunk()
        chunk.setTitle("Rasterization")
        chunk.setSummary("Converts 3D primitives to pixels")
        chunk.setKey("rasterization")
        chunk.setKeywords(["pipeline"])
        chunk.setQuestions(["What is rasterization?"])
        chunk.setRelatedKeys([])
        chunk.setEntities([])
        chunk
    }

    def "returns enriched chunk with token counts on successful response"() {
        given:
        def chatResponse = buildChatResponse(RAW_JSON, 20, 8)
        callSpec.chatResponse() >> chatResponse
        chunkDeserializer.readValue(RAW_JSON, Chunk.class) >> baseChunk()

        when:
        def result = client.enrich(null, TO_ENRICH, ROLLING_KEYS)

        then:
        result.tokenCountInput == 20
        result.tokenCountOutput == 8
        result.key == "rasterization"
    }

    def "passes previousSummary to prompt template when provided"() {
        given:
        def chatResponse = buildChatResponse(RAW_JSON)
        callSpec.chatResponse() >> chatResponse
        chunkDeserializer.readValue(RAW_JSON, Chunk.class) >> baseChunk()

        when:
        client.enrich("Previous page summary", TO_ENRICH, ROLLING_KEYS)

        then:
        1 * promptTemplate.render({ Map m ->
            m["prev_summary"] == "Previous page summary" &&
                    m["page_text"] == TO_ENRICH &&
                    m["rolling_keys"] == ROLLING_KEYS
        }) >> RENDERED_PROMPT
    }

    def "substitutes N/A for previousSummary when null"() {
        given:
        def chatResponse = buildChatResponse(RAW_JSON)
        callSpec.chatResponse() >> chatResponse
        chunkDeserializer.readValue(RAW_JSON, Chunk.class) >> baseChunk()

        when:
        client.enrich(null, TO_ENRICH, ROLLING_KEYS)

        then:
        1 * promptTemplate.render({ Map m -> m["prev_summary"] == "N/A" }) >> RENDERED_PROMPT
    }

    def "resets failure tracker after successful enrichment"() {
        given:
        def chatResponse = buildChatResponse(RAW_JSON)
        callSpec.chatResponse() >> chatResponse
        chunkDeserializer.readValue(RAW_JSON, Chunk.class) >> baseChunk()

        when:
        client.enrich(null, TO_ENRICH, ROLLING_KEYS)

        then:
        1 * failureTracker.resetFailures()
    }

    def "sets token counts from usage metadata"() {
        given:
        def chatResponse = buildChatResponse(RAW_JSON, 42, 17)
        callSpec.chatResponse() >> chatResponse
        chunkDeserializer.readValue(RAW_JSON, Chunk.class) >> baseChunk()

        when:
        def result = client.enrich(null, TO_ENRICH, ROLLING_KEYS)

        then:
        result.tokenCountInput == 42
        result.tokenCountOutput == 17
    }

    def "retries when chatResponse is null, succeeds on second attempt"() {
        given:
        def goodResponse = buildChatResponse(RAW_JSON)
        callSpec.chatResponse() >>> [null, goodResponse]
        chunkDeserializer.readValue(RAW_JSON, Chunk.class) >> baseChunk()

        when:
        def result = client.enrich(null, TO_ENRICH, ROLLING_KEYS)

        then:
        _ * failureTracker.registerFailure()
        result != null
    }

    def "retries when getResult returns null, succeeds on second attempt"() {
        given:
        def nullResultResponse = Mock(ChatResponse) { getResult() >> null }
        def goodResponse = buildChatResponse(RAW_JSON)
        callSpec.chatResponse() >>> [nullResultResponse, goodResponse]
        chunkDeserializer.readValue(RAW_JSON, Chunk.class) >> baseChunk()

        when:
        def result = client.enrich(null, TO_ENRICH, ROLLING_KEYS)

        then:
        _ * failureTracker.registerFailure()
        result != null
    }

    def "retries when getText returns null, succeeds on second attempt"() {
        given:
        def nullTextMessage = Mock(AssistantMessage) { getText() >> null }
        def nullTextGeneration = Mock(Generation) { getOutput() >> nullTextMessage }
        def nullTextResponse = Mock(ChatResponse) { getResult() >> nullTextGeneration }
        def goodResponse = buildChatResponse(RAW_JSON)
        callSpec.chatResponse() >>> [nullTextResponse, goodResponse]
        chunkDeserializer.readValue(RAW_JSON, Chunk.class) >> baseChunk()

        when:
        def result = client.enrich(null, TO_ENRICH, ROLLING_KEYS)

        then:
        _ * failureTracker.registerFailure()
        result != null
    }

    def "retries on JsonProcessingException and succeeds on second attempt"() {
        given:
        def chatResponse = buildChatResponse(RAW_JSON)
        callSpec.chatResponse() >> chatResponse
        def callCount = 0
        chunkDeserializer.readValue(RAW_JSON, Chunk.class) >> {
            if (callCount++ == 0) throw Mock(JsonProcessingException)
            baseChunk()
        }

        when:
        def result = client.enrich(null, TO_ENRICH, ROLLING_KEYS)

        then:
        1 * failureTracker.registerFailure()
        result != null
    }

    def "registers a failure for each JsonProcessingException before succeeding"() {
        given:
        def chatResponse = buildChatResponse(RAW_JSON)
        callSpec.chatResponse() >> chatResponse
        def callCount = 0
        chunkDeserializer.readValue(RAW_JSON, Chunk.class) >> {
            if (callCount++ < 2) throw Mock(JsonProcessingException)
            baseChunk()
        }

        when:
        client.enrich(null, TO_ENRICH, ROLLING_KEYS)

        then:
        2 * failureTracker.registerFailure()
    }

    def "FailureTracker exception propagates out of enrich"() {
        given:
        def chatResponse = buildChatResponse(RAW_JSON)
        callSpec.chatResponse() >> chatResponse
        chunkDeserializer.readValue(RAW_JSON, Chunk.class) >> { throw Mock(JsonProcessingException) }
        failureTracker.registerFailure() >> { throw new RuntimeException("Too many failures") }

        when:
        client.enrich(null, TO_ENRICH, ROLLING_KEYS)

        then:
        thrown(RuntimeException)
    }
}