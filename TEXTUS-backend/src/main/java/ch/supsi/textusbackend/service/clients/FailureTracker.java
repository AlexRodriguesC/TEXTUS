package ch.supsi.textusbackend.service.clients;

import ch.supsi.textusbackend.exception.BookDomainException;

public interface FailureTracker {

    void registerFailure() throws BookDomainException;

    void resetFailures();
}
