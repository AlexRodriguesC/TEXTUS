package ch.supsi.documentapplication.exception;

import org.springframework.http.HttpStatus;

public class RemoteCallFailedException extends ApplicationException {

    public RemoteCallFailedException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
