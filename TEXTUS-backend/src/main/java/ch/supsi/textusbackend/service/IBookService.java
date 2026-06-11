package ch.supsi.textusbackend.service;

import org.springframework.web.multipart.MultipartFile;

public interface IBookService {
    void createBook(MultipartFile book);
}
