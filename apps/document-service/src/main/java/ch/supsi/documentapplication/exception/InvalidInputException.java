package ch.supsi.documentapplication.exception;

import org.springframework.http.HttpStatus;

public class InvalidInputException extends ApplicationException {

    public InvalidInputException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public static InvalidInputException invalidSettings() {
        return new InvalidInputException("The provided settings are invalid.");
    }
}
