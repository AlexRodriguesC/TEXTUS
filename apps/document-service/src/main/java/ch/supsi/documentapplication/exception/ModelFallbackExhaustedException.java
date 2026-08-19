package ch.supsi.documentapplication.exception;

import org.springframework.ai.retry.TransientAiException;

/**
 * Raised when every configured model failed for a single outer retry attempt.
 * Extends {@link TransientAiException} so the Spring retry policy can start a fresh model cycle.
 */
public class ModelFallbackExhaustedException extends TransientAiException {

    public ModelFallbackExhaustedException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ModelFallbackExhaustedException create(int modelCount, String lastModel, Throwable cause) {
        return new ModelFallbackExhaustedException(
                "All " + modelCount + " configured models failed. Last model: '" + lastModel + "'.", cause);
    }
}
