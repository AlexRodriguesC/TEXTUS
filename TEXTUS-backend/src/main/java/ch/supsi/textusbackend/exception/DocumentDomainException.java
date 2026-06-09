package ch.supsi.textusbackend.exception;

public class DocumentDomainException extends RuntimeException {

    public DocumentDomainException(String message) {
        super(message);
    }

    public DocumentDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
