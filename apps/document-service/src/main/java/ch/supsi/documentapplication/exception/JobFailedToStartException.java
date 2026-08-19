package ch.supsi.documentapplication.exception;

import org.springframework.http.HttpStatus;

public class JobFailedToStartException extends ApplicationException {

    public JobFailedToStartException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
