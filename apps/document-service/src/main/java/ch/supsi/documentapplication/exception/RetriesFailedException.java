package ch.supsi.documentapplication.exception;

import org.springframework.http.HttpStatus;

public class RetriesFailedException extends ApplicationException {

    public RetriesFailedException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }

    public static RetriesFailedException failedToMapLLMOutput() {
        return new RetriesFailedException("Exhausted retries to map LLM output.");
    }
}
