package ch.supsi.documentapplication.service.documentpipeline;

import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage;
import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage;
import ch.supsi.documentapplication.model.Chunk;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@StepScope
public class JSONOutputTasklet extends ProcessingTasklet {

    private static final String CHUNK_IDS = "CHUNK_IDS";

    private final IDatabaseDocumentStorage databaseDocumentStorage;
    private final IFileSystemDocumentStorage fileSystemDocumentStorage;

    @Autowired
    public JSONOutputTasklet(
            IDatabaseDocumentStorage databaseDocumentStorage, IFileSystemDocumentStorage fileSystemDocumentStorage) {
        this.databaseDocumentStorage = databaseDocumentStorage;
        this.fileSystemDocumentStorage = fileSystemDocumentStorage;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext) {
        log.debug("Executing JSON output step for document with documentId: {}", documentId);

        var jobExecutionContext = chunkContext.getStepContext().getJobExecutionContext();
        List<Long> chunkIds = (List<Long>) jobExecutionContext.getOrDefault(CHUNK_IDS, new ArrayList<>());

        if (chunkIds.isEmpty()) {
            log.warn("No chunk IDs found for document with documentId: {}", documentId);
        }

        List<Chunk> chunks =
                chunkIds.stream().map(databaseDocumentStorage::read).toList();
        fileSystemDocumentStorage.createChunkJSON(chunks, getDocumentIdAsUUID());

        return RepeatStatus.FINISHED;
    }
}
