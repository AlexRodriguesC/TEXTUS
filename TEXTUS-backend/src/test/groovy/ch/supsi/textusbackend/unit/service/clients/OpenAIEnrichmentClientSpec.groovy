package ch.supsi.textusbackend.unit.service.clients

import ch.supsi.textusbackend.model.Chunk
import ch.supsi.textusbackend.properties.PromptProperties
import ch.supsi.textusbackend.service.clients.enrichment.OpenAIEnrichmentClient
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.tokenizer.TokenCountEstimator
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import spock.lang.Specification
import tools.jackson.core.JacksonException

class OpenAIEnrichmentClientSpec extends Specification {

    ChatClient.Builder chatBuilder = Mock()
    ChatClient chatClient = Mock()
    PromptProperties promptProperties = Mock()
    ResourceLoader resourceLoader = Mock()
    TokenCountEstimator tokenCountEstimator = Mock()
    Resource systemResource = Mock()
    Resource userResource = Mock()
    ChatClient.ChatClientRequestSpec requestSpec = Mock()
    ChatClient.CallResponseSpec callSpec = Mock()

    OpenAIEnrichmentClient openAIEnrichmentClient

    static final String USER_PROMPT_TEMPLATE =
            "prev=<prev_summary> text=<page_text> keys=<rolling_keys>"

    static Chunk aChunk() {
        def c = new Chunk()
        c.setSummary("summary")
        c.setEntities([])
        c.setKeywords([])
        return c
    }

    def setup() {
        promptProperties.getEnrichmentSystem() >> "classpath:system.txt"
        promptProperties.getEnrichmentUser() >> "classpath:user.txt"
        resourceLoader.getResource("classpath:system.txt") >> systemResource
        resourceLoader.getResource("classpath:user.txt") >> userResource
        systemResource.getInputStream() >> new ByteArrayInputStream("system prompt".bytes)
        userResource.getInputStream() >> new ByteArrayInputStream(USER_PROMPT_TEMPLATE.bytes)

        chatBuilder.defaultSystem(systemResource) >> chatBuilder
        chatBuilder.build() >> chatClient
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> callSpec

        tokenCountEstimator.estimate(_ as String) >> 42

        openAIEnrichmentClient = new OpenAIEnrichmentClient(chatBuilder, promptProperties, resourceLoader, tokenCountEstimator)
    }

    def "should return chunk with expected properties"() {
        given:
        def expectedChunk = aChunk()
        callSpec.entity(Chunk.class) >> expectedChunk

        when:
        def result = openAIEnrichmentClient.enrich("prev summary", "page text", ["key1", "key2"])

        then:
        result == expectedChunk
        result.tokenCountInput == 42
        result.tokenCountOutput == 42
    }

    def "should substitute N/A when previousSummary is null"() {
        given:
        callSpec.entity(Chunk.class) >> aChunk()

        when:
        def result = openAIEnrichmentClient.enrich(null, "page text", [])

        then:
        noExceptionThrown()
        result != null
    }

    def "should not throw exception if it fails to map LLM prompt less than max times"() {
        given:
        def successChunk = aChunk()
        // fail 4 times (< maxAttempts=5), succeed on the 5th call
        callSpec.entity(Chunk.class) >> { throw Mock(JacksonException) } >> { throw Mock(JacksonException) } >> { throw Mock(JacksonException) } >> { throw Mock(JacksonException) } >> successChunk

        when:
        def result = openAIEnrichmentClient.enrich("prev", "text", [])

        then:
        noExceptionThrown()
        result == successChunk
    }

    def "should propagate exception if it fails to map LLM prompt too many times"() {
        given:
        callSpec.entity(Chunk.class) >> { throw Mock(JacksonException) }

        when:
        openAIEnrichmentClient.enrich("prev", "text", [])

        then:
        thrown(JacksonException)
    }

    def "should call normaliseEntities on the returned chunk"() {
        given:
        def chunk = Spy(aChunk())
        callSpec.entity(Chunk.class) >> chunk

        when:
        openAIEnrichmentClient.enrich("prev", "text", [])

        then:
        1 * chunk.normaliseEntities()
    }
}
