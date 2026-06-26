package ch.supsi.documentapplication.service.document;

import ch.supsi.documentapplication.model.Document;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IDocumentService {
    void createDocument(MultipartFile document, String owner, Long spaceId);

    List<Document> getDocuments(String owner, @NotNull Long spaceId);
}
