package ch.supsi.documentapplication.service.async.mindmappipeline;

import ch.supsi.documentapplication.dataaccess.DatabaseDocumentStorage;
import ch.supsi.documentapplication.dataaccess.DatabaseTopicStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseAiUsageStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage;
import ch.supsi.documentapplication.model.*;
import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.service.async.ProcessingTasklet;
import ch.supsi.documentapplication.service.clients.OpenAIMindMapClient;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.retry.RetryException;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@StepScope
public class GenerationTasklet extends ProcessingTasklet {

    private final DatabaseTopicStorage databaseTopicStorage;
    private final DatabaseDocumentStorage databaseDocumentStorage;
    private final IDatabaseSpaceStorage databaseSpaceStorage;
    private final OpenAIMindMapClient mindMapClient;
    private final IDatabaseAiUsageStorage aiUsageStorage;

    @Autowired
    public GenerationTasklet(
            IDatabaseSpaceStorage databaseSpaceStorage,
            OpenAIMindMapClient mindMapClient,
            DatabaseDocumentStorage databaseDocumentStorage,
            DatabaseTopicStorage databaseTopicStorage,
            IDatabaseAiUsageStorage aiUsageStorage) {
        this.databaseSpaceStorage = databaseSpaceStorage;
        this.mindMapClient = mindMapClient;
        this.databaseDocumentStorage = databaseDocumentStorage;
        this.databaseTopicStorage = databaseTopicStorage;
        this.aiUsageStorage = aiUsageStorage;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext)
            throws RetryException {

        Long mapId =
                (Long) chunkContext.getStepContext().getJobExecutionContext().get("MAP_ID");

        var documents = databaseDocumentStorage.getDocuments(spaceId);
        List<Page> pages = new ArrayList<>();
        List<Topic> topics = new ArrayList<>();
        for (Document document : documents) {
            var documentPages = databaseDocumentStorage.getPages(document.getId());
            pages.addAll(documentPages);
            for (Page page : documentPages) {
                topics.addAll(databaseTopicStorage.getByPageId(page.getId()));
            }
        }

        if (pages.isEmpty() || topics.isEmpty()) {
            contribution.setExitStatus(ExitStatus.FAILED);
            return RepeatStatus.FINISHED;
        }

        var mindMapResult = mindMapClient.getResult(new MindMapGenerationInput(pages, topics));

        databaseSpaceStorage.createMindMapNodes(spaceId, mapId, mindMapResult.result());
        persistMindMapUsage(mapId, mindMapResult);

        return RepeatStatus.FINISHED;
    }

    private void persistMindMapUsage(Long mapId, ClientResult<MindMap> result) {
        for (int i = 0; i < result.usages().size(); i++) {
            String operation = result.usageOperations() != null
                            && i < result.usageOperations().size()
                    ? result.usageOperations().get(i)
                    : "mind_map_generate";
            aiUsageStorage.create("mind_map", mapId, operation, result.usages().get(i));
        }
    }
}
