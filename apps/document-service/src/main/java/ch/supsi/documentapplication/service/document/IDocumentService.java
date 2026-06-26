package ch.supsi.documentapplication.service.document;

import ch.supsi.documentapplication.model.DocumentStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IDocumentService {
    void createDocument(MultipartFile document, String owner, Long spaceId);

    List<DocumentStatus> getDocumentStatuses(String owner, @NotNull Long spaceId);
}
