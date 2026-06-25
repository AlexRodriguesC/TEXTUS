package ch.supsi.documentapplication.exception;

public class DocumentDomainException extends RuntimeException {

    public DocumentDomainException(String message) {
        super(message);
    }

    public DocumentDomainException(String message, Throwable cause) {
        super(message, cause);
    }

    public static DocumentDomainException jobFailedException(Throwable cause) {
        return new DocumentDomainException("Failed to start asynchronous job", cause);
    }
}
