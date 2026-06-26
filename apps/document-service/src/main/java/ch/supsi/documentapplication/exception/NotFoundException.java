package ch.supsi.documentapplication.exception;

import org.jetbrains.annotations.NotNull;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException spaceNotFoundException(Long spaceId) {
        return new NotFoundException("Space with id " + spaceId + " not found");
    }

    public static NotFoundException spaceOrDocumentNotFoundException(Long spaceId, Long documentId) {
        return new NotFoundException("Space with id " + spaceId + " or document " + documentId + " not found");
    }

    public static NotFoundException mindMapNotFoundException(Long spaceId) {
        return new NotFoundException("Mind map for space with id " + spaceId + " not found");
    }

    public static @NotNull RuntimeException userNotFound(String owner) {
        return new NotFoundException("User " + owner + " not found");
    }
}
