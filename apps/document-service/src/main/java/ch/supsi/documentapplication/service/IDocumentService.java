package ch.supsi.documentapplication.service;

import org.springframework.web.multipart.MultipartFile;

public interface IDocumentService {
    void parseDocument(MultipartFile document);
}
