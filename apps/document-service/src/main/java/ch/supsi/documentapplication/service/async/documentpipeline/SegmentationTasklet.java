package ch.supsi.documentapplication.service.async.documentpipeline;

import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage;
import ch.supsi.documentapplication.exception.FileSystemException;
import ch.supsi.documentapplication.model.FsDocument;
import ch.supsi.documentapplication.model.TaskType;
import ch.supsi.documentapplication.service.async.ProcessingTasklet;
import ch.supsi.documentapplication.service.document.DocumentTaskProgressService;
import ch.supsi.documentapplication.service.pdf.PdfPageTextExtractor;
import java.io.IOException;
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
public class SegmentationTasklet extends ProcessingTasklet {

    private final IFileSystemDocumentStorage fileSystemDocumentStorage;
    private final DocumentTaskProgressService documentTaskProgressService;

    @Autowired
    public SegmentationTasklet(
            IFileSystemDocumentStorage fileSystemDocumentStorage,
            DocumentTaskProgressService documentTaskProgressService) {
        this.fileSystemDocumentStorage = fileSystemDocumentStorage;
        this.documentTaskProgressService = documentTaskProgressService;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext)
            throws Exception {

        Long databaseDocumentId = getDatabaseDocumentId(chunkContext);
        FsDocument fsDocument = fileSystemDocumentStorage.read(getDocumentIdAsUUID());

        if (fsDocument == null) {
            String message = String.format("Document with id %s not found", documentId);
            throw new FileSystemException(message);
        }

        List<String> contents = extractContents(fsDocument);
        documentTaskProgressService.startTask(databaseDocumentId, TaskType.SEGMENTATION, contents.size());

        for (int page = 0; page < contents.size(); page++) {
            String pageText = contents.get(page);
            Integer pageNumber = page + 1;
            if (fileSystemDocumentStorage.chunkExists(getDocumentIdAsUUID(), pageNumber)) {
                log.debug("Chunk for page {} already exists for document {}, skipping", pageNumber, documentId);
            } else {
                String pageId = fileSystemDocumentStorage.createChunk(pageNumber, pageText, getDocumentIdAsUUID());
                log.debug("Created page {} for document {}, page {}", pageId, documentId, page);
            }
            documentTaskProgressService.updateProgress(databaseDocumentId, TaskType.SEGMENTATION, page + 1);
        }

        documentTaskProgressService.completeTask(databaseDocumentId, TaskType.SEGMENTATION);
        return RepeatStatus.FINISHED;
    }

    private List<String> extractContents(FsDocument fsDocument) throws IOException {
        return PdfPageTextExtractor.extractPageTexts(fsDocument.getBytes());
    }
}
