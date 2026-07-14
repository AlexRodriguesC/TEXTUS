package ch.supsi.documentapplication.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApplicationException {

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public static NotFoundException spaceNotFoundException(Long spaceId) {
        return new NotFoundException(format("Space with id %d not found", spaceId));
    }

    public static NotFoundException documentNotFoundException(Long documentId) {
        return new NotFoundException(format("Document with id %d not found", documentId));
    }

    public static NotFoundException spaceOrDocumentNotFoundException(Long spaceId, Long documentId) {
        return new NotFoundException(format("Space with id %d or document %d not found", spaceId, documentId));
    }

    public static NotFoundException mindMapNotFoundException(Long spaceId) {
        return new NotFoundException(format("Mind map for space with id %d not found", spaceId));
    }

    public static NotFoundException userNotFound(String owner) {
        return new NotFoundException(format("User %s not found", owner));
    }

    public static NotFoundException learningPathNotFound(Long spaceId) {
        return new NotFoundException(format("Learning path for space with id %d not found", spaceId));
    }
}
