package ch.supsi.documentapplication.exception;

import org.springframework.http.HttpStatus;

public class RetriesFailedException extends ApplicationException {

    public RetriesFailedException(String message, Throwable cause) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message, cause);
    }

    public static RetriesFailedException failedToMapLLMOutput(Throwable cause) {
        return new RetriesFailedException("Exhausted retries to map LLM output.", cause);
    }
}
