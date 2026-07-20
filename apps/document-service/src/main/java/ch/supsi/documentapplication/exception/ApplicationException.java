package ch.supsi.documentapplication.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApplicationException extends RuntimeException {

    private final HttpStatus httpStatus;

    public ApplicationException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public ApplicationException(HttpStatus httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public static ApplicationException internalServerError(String message) {
        return new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    protected static String format(String template, Object... args) {
        return String.format(template, args);
    }
}
