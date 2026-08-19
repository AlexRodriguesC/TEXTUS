package ch.supsi.documentapplication.exception;

import org.springframework.http.HttpStatus;

public class UsernameTakenException extends ApplicationException {

    public UsernameTakenException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
