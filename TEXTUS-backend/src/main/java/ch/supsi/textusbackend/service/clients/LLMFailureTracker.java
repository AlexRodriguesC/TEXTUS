package ch.supsi.textusbackend.service.clients;

import ch.supsi.textusbackend.exception.DocumentDomainException;
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
    public void registerFailure() throws DocumentDomainException {
        currentFailures++;

        if (currentFailures >= maxFailures) {
            log.error("Failed to extract LLM response {} times", currentFailures);
            throw new DocumentDomainException("Failed to extract LLM response " + currentFailures + " times");
        }
    }

    @Override
    public void resetFailures() {
        currentFailures = 0;
    }
}
