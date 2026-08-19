package ch.supsi.documentapplication.unit.service.rag

import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage
import ch.supsi.documentapplication.model.learn.ExerciseType
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.service.rag.FallbackTopicDocumentPostProcessor
import org.springframework.ai.document.Document
import org.springframework.ai.rag.Query
import spock.lang.Specification

class FallbackTopicDocumentPostProcessorSpec extends Specification {

    IDatabaseTopicStorage topicStorage = Mock()
    Long spaceId = 7L

    FallbackTopicDocumentPostProcessor processor = new FallbackTopicDocumentPostProcessor(topicStorage, spaceId, 3)

    def "returns retrieved documents unchanged when non-empty"() {
        given:
        def existing = [Document.builder().text("matched").build()]
        def query = Query.builder().text("question").build()

        when:
        def result = processor.process(query, existing)

        then:
        result == existing
        0 * topicStorage._
    }

    def "injects synthetic fallback documents when retrieval is empty"() {
        given:
        def query = Query.builder().text("question").build()
        def topics = [
                new Topic(1L, null, 10L, 1, "Topic One", "Summary one", [], [ExerciseType.QUESTION]),
                new Topic(2L, null, 11L, 1, "Topic Two", null, [], [ExerciseType.QUESTION]),
        ]

        when:
        def result = processor.process(query, [])

        then:
        1 * topicStorage.findRandomTopicsBySpace(spaceId, 3) >> topics
        result.size() == 2
        result*.getText() == ["Topic One: Summary one", "Topic Two"]
        result.every { it.metadata.get("fallback") == true }
        result.every { it.metadata.get("spaceId") == spaceId }
        result.collect { it.metadata.get("topicId") } == [1L, 2L]
    }

    def "returns empty list when space has no topics"() {
        given:
        def query = Query.builder().text("question").build()

        when:
        def result = processor.process(query, [])

        then:
        1 * topicStorage.findRandomTopicsBySpace(spaceId, 3) >> []
        result.isEmpty()
    }
}
