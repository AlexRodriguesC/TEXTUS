package ch.supsi.documentapplication.service.rag;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import org.springframework.ai.document.Document;

public final class TopicGroundingExtractor {

    private TopicGroundingExtractor() {}

    public static List<Long> extractTopicIds(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return List.of();
        }
        TreeSet<Long> topicIds = new TreeSet<>();
        for (Document document : documents) {
            if (Boolean.TRUE.equals(document.getMetadata().get(FallbackTopicDocumentPostProcessor.METADATA_FALLBACK))) {
                continue;
            }
            Object topicId = document.getMetadata().get(FallbackTopicDocumentPostProcessor.METADATA_TOPIC_ID);
            if (topicId instanceof Number number) {
                topicIds.add(number.longValue());
            }
        }
        return new ArrayList<>(topicIds);
    }
}
