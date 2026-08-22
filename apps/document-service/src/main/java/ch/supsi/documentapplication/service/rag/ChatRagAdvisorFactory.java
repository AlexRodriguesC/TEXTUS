package ch.supsi.documentapplication.service.rag;

import ch.supsi.documentapplication.configuration.properties.ChatRagProperties;
import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage;
import java.util.List;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class ChatRagAdvisorFactory {

    private final VectorStore vectorStore;
    private final IDatabaseTopicStorage topicStorage;
    private final ChatRagProperties ragProperties;
    private final QueryTransformer compressionQueryTransformer;
    private final QueryTransformer rewriteQueryTransformer;
    private final QueryAugmenter queryAugmenter;

    public ChatRagAdvisorFactory(
            VectorStore vectorStore,
            IDatabaseTopicStorage topicStorage,
            ChatRagProperties ragProperties,
            CompressionQueryTransformer compressionQueryTransformer,
            RewriteQueryTransformer rewriteQueryTransformer,
            QueryAugmenter queryAugmenter) {
        this.vectorStore = vectorStore;
        this.topicStorage = topicStorage;
        this.ragProperties = ragProperties;
        this.compressionQueryTransformer = compressionQueryTransformer;
        this.rewriteQueryTransformer = rewriteQueryTransformer;
        this.queryAugmenter = queryAugmenter;
    }

    public Advisor createForSpace(Long spaceId) {
        return createForSpace(spaceId, List.of());
    }

    public Advisor createForSpace(Long spaceId, List<Long> pinnedTopicIds) {
        var documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(ragProperties.getSimilarityThreshold())
                .topK(ragProperties.getTopK())
                .filterExpression(() -> ChatRagFilterExpressions.spaceIdEquals(spaceId))
                .build();

        var fallbackProcessor =
                new FallbackTopicDocumentPostProcessor(topicStorage, spaceId, ragProperties.getFallbackTopicLimit());

        var builder = RetrievalAugmentationAdvisor.builder()
                .queryTransformers(compressionQueryTransformer, rewriteQueryTransformer)
                .documentRetriever(documentRetriever)
                .queryAugmenter(queryAugmenter);

        if (!CollectionUtils.isEmpty(pinnedTopicIds)) {
            var pinnedProcessor = new PinnedTopicDocumentPostProcessor(topicStorage, spaceId, pinnedTopicIds);
            builder.documentPostProcessors(pinnedProcessor, fallbackProcessor);
        } else {
            builder.documentPostProcessors(fallbackProcessor);
        }

        return builder.build();
    }
}
