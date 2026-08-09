package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.exception.AIOutputMappingFailedException;
import com.openai.errors.RateLimitException;
import java.util.Set;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public final class ModelFallbackClassifier {

    private static final Set<Integer> FALLBACK_HTTP_STATUSES = Set.of(429, 502, 503);

    private ModelFallbackClassifier() {}

    public static boolean isFallbackEligible(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (isFallbackEligibleThrowable(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static boolean isFallbackEligibleThrowable(Throwable throwable) {
        if (throwable instanceof TransientAiException
                || throwable instanceof AIOutputMappingFailedException
                || throwable instanceof RateLimitException
                || throwable instanceof ResourceAccessException) {
            return true;
        }
        if (throwable instanceof NonTransientAiException nonTransientAiException) {
            return indicatesProviderUnavailability(nonTransientAiException.getMessage());
        }
        Integer status = extractHttpStatus(throwable);
        return status != null && FALLBACK_HTTP_STATUSES.contains(status);
    }

    private static boolean indicatesProviderUnavailability(String message) {
        if (message == null) {
            return false;
        }
        String normalized = message.toLowerCase();
        return normalized.contains("429")
                || normalized.contains("rate limit")
                || normalized.contains("502")
                || normalized.contains("503")
                || normalized.contains("unavailable")
                || normalized.contains("overloaded");
    }

    private static Integer extractHttpStatus(Throwable throwable) {
        if (throwable instanceof WebClientResponseException webClientResponseException) {
            return webClientResponseException.getStatusCode().value();
        }
        if (throwable instanceof RestClientResponseException restClientResponseException) {
            return restClientResponseException.getStatusCode().value();
        }
        return null;
    }
}
