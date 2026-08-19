package ch.supsi.documentapplication.exception;

import org.springframework.http.HttpStatus;

public class FileSystemException extends ApplicationException {

    public FileSystemException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
