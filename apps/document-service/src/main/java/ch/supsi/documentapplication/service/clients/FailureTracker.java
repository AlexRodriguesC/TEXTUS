package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.exception.LLMExtractionFailedException;

public interface FailureTracker {

    void registerFailure() throws LLMExtractionFailedException;

    void resetFailures();
}
