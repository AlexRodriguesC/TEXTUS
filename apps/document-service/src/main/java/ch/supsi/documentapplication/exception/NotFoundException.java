package ch.supsi.documentapplication.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException spaceNotFoundException(Long spaceId) {
        return new NotFoundException("Space with id " + spaceId + " not found");
    }
}
