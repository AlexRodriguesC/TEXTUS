package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.*;
import java.util.List;

public interface IDatabaseDocumentStorage {

    Long create(Page page, Long spaceId, Long documentId);

    Long create(Long spaceId, Long jobId);

    List<Document> getDocuments(Long spaceId);

    List<Page> getPages(Long documentId);
}
