package ch.supsi.documentapplication.unit.service.rag

import ch.supsi.documentapplication.service.rag.FallbackTopicDocumentPostProcessor
import ch.supsi.documentapplication.service.rag.TopicGroundingExtractor
import org.springframework.ai.document.Document
import spock.lang.Specification

class TopicGroundingExtractorSpec extends Specification {

    def "returns empty list for null or empty documents"() {
        expect:
        TopicGroundingExtractor.extractTopicIds(null) == []
        TopicGroundingExtractor.extractTopicIds([]) == []
    }

    def "extracts distinct sorted topic ids from non-fallback documents"() {
        given:
        def documents = [
                topicDocument(3L, false),
                topicDocument(1L, false),
                topicDocument(1L, false),
                topicDocument(2L, false),
        ]

        expect:
        TopicGroundingExtractor.extractTopicIds(documents) == [1L, 2L, 3L]
    }

    def "excludes fallback documents"() {
        given:
        def documents = [
                topicDocument(10L, false),
                topicDocument(20L, true),
        ]

        expect:
        TopicGroundingExtractor.extractTopicIds(documents) == [10L]
    }

    def "ignores documents without topic id metadata"() {
        given:
        def documents = [Document.builder().text("no metadata").build()]

        expect:
        TopicGroundingExtractor.extractTopicIds(documents) == []
    }

    private static Document topicDocument(Long topicId, boolean fallback) {
        def metadata = [(FallbackTopicDocumentPostProcessor.METADATA_TOPIC_ID): topicId]
        if (fallback) {
            metadata[FallbackTopicDocumentPostProcessor.METADATA_FALLBACK] = true
        }
        Document.builder().text("topic $topicId").metadata(metadata).build()
    }
}
