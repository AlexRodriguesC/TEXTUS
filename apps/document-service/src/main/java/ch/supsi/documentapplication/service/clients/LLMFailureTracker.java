package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.exception.LLMExtractionFailedException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LLMFailureTracker implements FailureTracker {

    private Integer currentFailures;
    private final Integer maxFailures;

    private LLMFailureTracker(Integer maxFailures) {
        this.currentFailures = 0;
        this.maxFailures = maxFailures;
    }

    public static LLMFailureTracker create(Integer maxFailures) {
        if (maxFailures == null || maxFailures <= 0) {
            throw new IllegalArgumentException("maxFailures must be greater than 0");
        }
        return new LLMFailureTracker(maxFailures);
    }

    @Override
    public void registerFailure() throws LLMExtractionFailedException {
        currentFailures++;
        log.warn("Registering failure");

        if (currentFailures >= maxFailures) {
            String message = String.format("Failed to extract LLM response %d times", currentFailures);
            resetFailures();
            log.error(message);
            throw new LLMExtractionFailedException(message);
        }
    }

    @Override
    public void resetFailures() {
        currentFailures = 0;
    }
}
