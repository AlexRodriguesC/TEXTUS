package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.exception.RetriesFailedException;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.core.retry.Retryable;

@Slf4j
public class RetryTemplateWrapper implements org.springframework.core.retry.RetryOperations {

    private final RetryTemplate retryTemplate;

    public RetryTemplateWrapper(RetryTemplate retryTemplate) {
        this.retryTemplate = retryTemplate;
    }

    @Override
    public <R> R execute(@NonNull Retryable<R> retryable) throws RetriesFailedException {
        try {
            return retryTemplate.execute(retryable);
        } catch (RetryException e) {
            log.error("Retries failed", e);
            throw RetriesFailedException.failedToMapLLMOutput(e.getCause());
        }
    }

    @Override
    public <R> R invoke(@NonNull Supplier<R> retryable) {
        return retryTemplate.invoke(retryable);
    }

    @Override
    public void invoke(@NonNull Runnable retryable) {
        retryTemplate.invoke(retryable);
    }
}
