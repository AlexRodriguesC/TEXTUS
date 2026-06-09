package ch.supsi.textusbackend.service.clients;

import ch.supsi.textusbackend.exception.DocumentDomainException;

public interface FailureTracker {

    void registerFailure() throws DocumentDomainException;

    void resetFailures();
}
