package ch.supsi.documentapplication.exception;

import org.springframework.ai.retry.TransientAiException;

public class AIOutputMappingFailedException extends TransientAiException {
    public AIOutputMappingFailedException(String message) {
        super(message);
    }
}
