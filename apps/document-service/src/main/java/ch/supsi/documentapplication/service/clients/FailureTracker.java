package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.exception.DocumentDomainException;

public interface FailureTracker {

    void registerFailure() throws DocumentDomainException;

    void resetFailures();
}
