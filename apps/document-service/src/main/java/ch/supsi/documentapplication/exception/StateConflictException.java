package ch.supsi.documentapplication.exception;

import org.springframework.http.HttpStatus;

public class StateConflictException extends ApplicationException {

    public StateConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }

    public static StateConflictException mindMapAlreadyGeneratedException(Long documentId) {
        return new StateConflictException(
                "Document with id " + documentId + " cannot be deleted because a mind map was already generated");
    }
}
