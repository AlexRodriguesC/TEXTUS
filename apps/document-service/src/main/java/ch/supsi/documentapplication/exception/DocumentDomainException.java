package ch.supsi.documentapplication.exception;

public class DocumentDomainException extends RuntimeException {

    public DocumentDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
