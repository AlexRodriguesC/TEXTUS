package ch.supsi.documentapplication.unit.service.rag

import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage
import ch.supsi.documentapplication.model.learn.ExerciseType
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.service.rag.FallbackTopicDocumentPostProcessor
import ch.supsi.documentapplication.service.rag.PinnedTopicDocumentPostProcessor
import org.springframework.ai.document.Document
import org.springframework.ai.rag.Query
import spock.lang.Specification

class PinnedTopicDocumentPostProcessorSpec extends Specification {

    IDatabaseTopicStorage topicStorage = Mock()
    Long spaceId = 7L

    def "returns retrieved documents unchanged when no pinned ids"() {
        given:
        def processor = new PinnedTopicDocumentPostProcessor(topicStorage, spaceId, [])
        def existing = [Document.builder().text("matched").build()]
        def query = Query.builder().text("question").build()

        when:
        def result = processor.process(query, existing)

        then:
        result == existing
        0 * topicStorage._
    }

    def "injects pinned topics when retrieval is empty"() {
        given:
        def processor = new PinnedTopicDocumentPostProcessor(topicStorage, spaceId, [10L])
        def query = Query.builder().text("question").build()
        def topic = new Topic(10L, null, 1L, 1, "Photosynthesis", "Plants convert light to energy", ["biology"], [ExerciseType.QUESTION])

        when:
        def result = processor.process(query, [])

        then:
        1 * topicStorage.getByIds(spaceId, [10L]) >> [topic]
        result.size() == 1
        result[0].getText().startsWith(PinnedTopicDocumentPostProcessor.PRIMARY_TOPIC_PREFIX)
        result[0].getText().contains("Photosynthesis")
        result[0].metadata.get(PinnedTopicDocumentPostProcessor.METADATA_PINNED) == true
        result[0].metadata.get(FallbackTopicDocumentPostProcessor.METADATA_TOPIC_ID) == 10L
        result[0].metadata.get(FallbackTopicDocumentPostProcessor.METADATA_FALLBACK) == null
    }

    def "prepends pinned topics and dedupes overlapping retrieved docs"() {
        given:
        def processor = new PinnedTopicDocumentPostProcessor(topicStorage, spaceId, [10L])
        def query = Query.builder().text("question").build()
        def topic = new Topic(10L, null, 1L, 1, "Pinned", "Pinned summary", [], [ExerciseType.QUESTION])
        def retrievedPinned = Document.builder()
                .text("vector hit for pinned")
                .metadata([(FallbackTopicDocumentPostProcessor.METADATA_TOPIC_ID): 10L])
                .build()
        def retrievedOther = Document.builder()
                .text("neighbor")
                .metadata([(FallbackTopicDocumentPostProcessor.METADATA_TOPIC_ID): 20L])
                .build()

        when:
        def result = processor.process(query, [retrievedPinned, retrievedOther])

        then:
        1 * topicStorage.getByIds(spaceId, [10L]) >> [topic]
        result.size() == 2
        result[0].metadata.get(PinnedTopicDocumentPostProcessor.METADATA_PINNED) == true
        result[0].metadata.get(FallbackTopicDocumentPostProcessor.METADATA_TOPIC_ID) == 10L
        result[1].getText() == "neighbor"
        result[1].metadata.get(FallbackTopicDocumentPostProcessor.METADATA_TOPIC_ID) == 20L
    }

    def "falls through to retrieved docs when pinned ids resolve to nothing"() {
        given:
        def processor = new PinnedTopicDocumentPostProcessor(topicStorage, spaceId, [999L])
        def query = Query.builder().text("question").build()
        def existing = [Document.builder().text("matched").build()]

        when:
        def result = processor.process(query, existing)

        then:
        1 * topicStorage.getByIds(spaceId, [999L]) >> []
        result == existing
    }
}
