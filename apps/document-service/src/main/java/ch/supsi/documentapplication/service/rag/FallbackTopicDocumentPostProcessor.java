package ch.supsi.documentapplication.service.rag;

import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage;
import ch.supsi.documentapplication.model.learn.Topic;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.util.StringUtils;

public class FallbackTopicDocumentPostProcessor implements DocumentPostProcessor {

    public static final String METADATA_FALLBACK = "fallback";
    public static final String METADATA_TOPIC_ID = "topicId";
    public static final String METADATA_SPACE_ID = "spaceId";

    private final IDatabaseTopicStorage topicStorage;
    private final Long spaceId;
    private final int fallbackLimit;

    public FallbackTopicDocumentPostProcessor(IDatabaseTopicStorage topicStorage, Long spaceId, int fallbackLimit) {
        this.topicStorage = topicStorage;
        this.spaceId = spaceId;
        this.fallbackLimit = fallbackLimit;
    }

    @Override
    public List<Document> process(Query query, List<Document> documents) {
        if (documents != null && !documents.isEmpty()) {
            return documents;
        }
        return topicStorage.findRandomTopicsBySpace(spaceId, fallbackLimit).stream()
                .map(this::toFallbackDocument)
                .toList();
    }

    private Document toFallbackDocument(Topic topic) {
        String text = topic.getTitle();
        if (StringUtils.hasText(topic.getSummary())) {
            text = topic.getTitle() + ": " + topic.getSummary();
        }
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(METADATA_TOPIC_ID, topic.getId());
        metadata.put(METADATA_SPACE_ID, spaceId);
        metadata.put(METADATA_FALLBACK, true);
        return Document.builder().text(text).metadata(metadata).build();
    }
}
