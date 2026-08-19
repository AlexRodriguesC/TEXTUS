<<<<<<<< HEAD:apps/document-service/src/main/java/ch/supsi/documentapplication/service/processingpipeline/CleanupTasklet.java
package ch.supsi.documentapplication.service.processingpipeline;

import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage;
========
package ch.supsi.documentapplication.service.async.documentpipeline;

import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage;
import ch.supsi.documentapplication.service.async.ProcessingTasklet;
>>>>>>>> origin/feature/testing_service:apps/document-service/src/main/java/ch/supsi/documentapplication/service/async/documentpipeline/CleanupTasklet.java
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
public class CleanupTasklet extends ProcessingTasklet {

    private final IFileSystemDocumentStorage documentStorage;

    @Autowired
    public CleanupTasklet(IFileSystemDocumentStorage documentStorage) {
        this.documentStorage = documentStorage;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext) {
        log.debug("Executing cleanup step for document with documentId {}", documentId);

        documentStorage.delete(getDocumentIdAsUUID());

        return RepeatStatus.FINISHED;
    }
}
