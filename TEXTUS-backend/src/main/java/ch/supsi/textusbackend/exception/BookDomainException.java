package ch.supsi.textusbackend.exception;

public class BookDomainException extends RuntimeException {

    public BookDomainException(String message) {
        super(message);
    }

    public BookDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
