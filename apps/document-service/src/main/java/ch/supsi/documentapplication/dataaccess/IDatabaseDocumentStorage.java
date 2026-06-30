package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.Chunk;
import ch.supsi.documentapplication.model.Document;
import ch.supsi.documentapplication.model.DocumentStatus;
import ch.supsi.documentapplication.model.Page;
import java.util.List;

public interface IDatabaseDocumentStorage {

    Long create(Chunk chunk, Long spaceId, Long documentId);

    Long create(Page page, Long spaceId, Long documentId);

    Long create(Long spaceId);

    void updateDocumentStatus(Long documentId, DocumentStatus status);

    List<Document> getDocuments(Long spaceId);

    Chunk read(Long chunkId);
}
