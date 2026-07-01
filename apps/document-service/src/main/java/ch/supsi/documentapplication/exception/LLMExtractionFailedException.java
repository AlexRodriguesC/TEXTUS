package ch.supsi.documentapplication.exception;

public class LLMExtractionFailedException extends RuntimeException {
    public LLMExtractionFailedException(String message) {
        super(message);
    }
}
