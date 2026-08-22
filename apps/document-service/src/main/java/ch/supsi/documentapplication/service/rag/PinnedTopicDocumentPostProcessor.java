package ch.supsi.documentapplication.service.rag;

import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage;
import ch.supsi.documentapplication.model.learn.Topic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.util.StringUtils;

/**
 * Always injects the caller's pinned topics as primary context, then appends
 * retrieved neighbors whose topicId is not already pinned. When any pinned
 * document is produced, the list is non-empty so {@link FallbackTopicDocumentPostProcessor}
 * (registered after this) becomes a no-op.
 */
public class PinnedTopicDocumentPostProcessor implements DocumentPostProcessor {

    public static final String METADATA_PINNED = "pinned";
    public static final String PRIMARY_TOPIC_PREFIX = "Primary topic — ";

    private final IDatabaseTopicStorage topicStorage;
    private final Long spaceId;
    private final List<Long> pinnedTopicIds;

    public PinnedTopicDocumentPostProcessor(
            IDatabaseTopicStorage topicStorage, Long spaceId, List<Long> pinnedTopicIds) {
        this.topicStorage = topicStorage;
        this.spaceId = spaceId;
        this.pinnedTopicIds = pinnedTopicIds == null ? List.of() : List.copyOf(pinnedTopicIds);
    }

    @Override
    public @NonNull List<Document> process(@NonNull Query query, @NonNull List<Document> documents) {
        if (pinnedTopicIds.isEmpty()) {
            return documents;
        }

        List<Document> pinnedDocs = topicStorage.getByIds(spaceId, pinnedTopicIds).stream()
                .map(this::toPinnedDocument)
                .filter(doc -> StringUtils.hasText(doc.getText()))
                .toList();

        if (pinnedDocs.isEmpty()) {
            return documents;
        }

        Set<Long> pinnedIds = new HashSet<>();
        for (Document pinned : pinnedDocs) {
            Object topicId = pinned.getMetadata().get(FallbackTopicDocumentPostProcessor.METADATA_TOPIC_ID);
            if (topicId instanceof Number number) {
                pinnedIds.add(number.longValue());
            }
        }

        List<Document> merged = new ArrayList<>(pinnedDocs);
        for (Document retrieved : documents) {
            Object topicId = retrieved.getMetadata().get(FallbackTopicDocumentPostProcessor.METADATA_TOPIC_ID);
            if (topicId instanceof Number number && pinnedIds.contains(number.longValue())) {
                continue;
            }
            merged.add(retrieved);
        }
        return merged;
    }

    private Document toPinnedDocument(Topic topic) {
        String promptSource = topic.toPromptSource();
        String text = StringUtils.hasText(promptSource) ? PRIMARY_TOPIC_PREFIX + promptSource : "";
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(FallbackTopicDocumentPostProcessor.METADATA_TOPIC_ID, topic.getId());
        metadata.put(FallbackTopicDocumentPostProcessor.METADATA_SPACE_ID, spaceId);
        metadata.put(METADATA_PINNED, true);
        return Document.builder().text(text).metadata(metadata).build();
    }
}
