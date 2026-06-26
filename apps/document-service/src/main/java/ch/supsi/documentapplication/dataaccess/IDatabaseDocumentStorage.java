package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.Chunk;
import ch.supsi.documentapplication.model.Document;
import ch.supsi.documentapplication.model.DocumentStatus;

import java.util.List;

public interface IDatabaseDocumentStorage {

    Long create(Chunk chunk, Long spaceId, Long documentId);

    Long create(Document document, Long spaceId);

    void updateDocumentStatus(Long documentId, DocumentStatus status);

    List<DocumentStatus> getDocumentStatuses(Long spaceId);

    Chunk read(Long chunkId);
}
