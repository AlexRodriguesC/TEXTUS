package ch.supsi.documentapplication.exception;

import org.springframework.http.HttpStatus;

public class LLMExtractionFailedException extends ApplicationException {

    public LLMExtractionFailedException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
