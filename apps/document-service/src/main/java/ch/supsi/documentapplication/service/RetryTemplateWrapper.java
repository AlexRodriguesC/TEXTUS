package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.exception.ModelFallbackExhaustedException;
import ch.supsi.documentapplication.exception.RetriesFailedException;
import java.lang.reflect.UndeclaredThrowableException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.core.retry.Retryable;

@Slf4j
public class RetryTemplateWrapper {

    private final RetryTemplate retryTemplate;

    public RetryTemplateWrapper(RetryTemplate retryTemplate) {
        this.retryTemplate = retryTemplate;
    }

    public <R> R execute(@NonNull Retryable<R> retryable) throws RetriesFailedException {
        try {
            return retryTemplate.execute(retryable);
        } catch (RetryException e) {
            Throwable cause = e.getCause();
            // Spring Retry wraps non-retryable failures too; rethrow when no retry was attempted.
            if (e.getRetryCount() == 0) {
                rethrow(cause);
            }
            log.error("Retries failed", e);
            throw RetriesFailedException.exhausted(unwrapTerminalCause(cause));
        }
    }

    private static Throwable unwrapTerminalCause(Throwable cause) {
        if (cause instanceof ModelFallbackExhaustedException && cause.getCause() != null) {
            return cause.getCause();
        }
        return cause;
    }

    private static void rethrow(Throwable cause) {
        if (cause instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (cause instanceof Error error) {
            throw error;
        }
        throw new UndeclaredThrowableException(cause);
    }
}
