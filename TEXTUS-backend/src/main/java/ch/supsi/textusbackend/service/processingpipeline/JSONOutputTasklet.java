package ch.supsi.textusbackend.service.processingpipeline;

import ch.supsi.textusbackend.dataaccess.IDatabaseDocumentStorage;
import ch.supsi.textusbackend.dataaccess.IFileSystemDocumentStorage;
import ch.supsi.textusbackend.model.Chunk;
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

    private final IDatabaseDocumentStorage databaseBookStorage;
    private final IFileSystemDocumentStorage fileSystemBookStorage;

    @Autowired
    public JSONOutputTasklet(
            IDatabaseDocumentStorage databaseBookStorage, IFileSystemDocumentStorage fileSystemBookStorage) {
        this.databaseBookStorage = databaseBookStorage;
        this.fileSystemBookStorage = fileSystemBookStorage;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext) {
        log.debug("Executing JSON output step for book with bookId {}", documentId);

        List<Chunk> chunks = databaseBookStorage.readChunks(documentId);
        fileSystemBookStorage.createChunkJSON(chunks, documentId);

        return RepeatStatus.FINISHED;
    }
}
