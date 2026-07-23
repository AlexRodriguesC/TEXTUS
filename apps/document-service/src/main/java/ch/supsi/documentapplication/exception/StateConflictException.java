package ch.supsi.documentapplication.exception;

import org.springframework.http.HttpStatus;

public class StateConflictException extends ApplicationException {

    public StateConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }

    public static StateConflictException mindMapAlreadyGeneratedException(Long documentId) {
        return new StateConflictException("Document with id " + documentId
                + " cannot be deleted because a mind map was already generated. Delete the mind map first.");
    }

    public static StateConflictException learningPathAlreadyGeneratedException(Long documentId) {
        return new StateConflictException("Document with id " + documentId
                + " cannot be deleted because a learning path was already generated. Delete the learning path first.");
    }

    public static StateConflictException documentStillProcessing(Long documentId) {
        return new StateConflictException("Document with id " + documentId + " is still being processed");
    }

    public static StateConflictException documentNotRetryable(Long documentId) {
        return new StateConflictException("Document with id " + documentId + " cannot be retried in its current state");
    }
}
