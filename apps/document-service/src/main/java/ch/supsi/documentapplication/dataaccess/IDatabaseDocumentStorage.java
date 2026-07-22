package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.*;
import java.util.List;
import java.util.Optional;

public interface IDatabaseDocumentStorage {

    Long create(Page page, Long spaceId, Long documentId);

    Long create(Long spaceId, Long jobId, String documentName);

    Optional<Long> findIdByJobId(Long jobId);

    List<Document> getDocuments(Long spaceId);

    List<Page> getPages(Long documentId);

    List<SpacePage> getPagesBySpaceId(Long spaceId);

    void deleteDocument(Long spaceId, Long documentId);
}
