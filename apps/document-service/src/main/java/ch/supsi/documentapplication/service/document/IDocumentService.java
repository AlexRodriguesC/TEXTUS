package ch.supsi.documentapplication.service.document;

import org.springframework.web.multipart.MultipartFile;

public interface IDocumentService {
    void createDocument(MultipartFile document, String owner, Long spaceId);
}
