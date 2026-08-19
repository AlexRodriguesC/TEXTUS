package ch.supsi.documentapplication.exception;

import org.springframework.ai.retry.TransientAiException;

public class AIOutputMappingFailedException extends TransientAiException {
    public AIOutputMappingFailedException(String message) {
        super(message);
    }

    public static AIOutputMappingFailedException create(String target, String cause) {
        var message = String.format("Failed to validate %s, cause: %s", target, cause);
        return new AIOutputMappingFailedException(message);
    }
}
