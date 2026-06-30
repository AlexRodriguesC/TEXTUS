package ch.supsi.documentapplication.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public static UnauthorizedException userNotOwnerOfSpace(String username, Long spaceId) {
        return new UnauthorizedException(
                String.format("User %s is not the owner of space with id %d", username, spaceId));
    }

    public static UnauthorizedException userNotAuthorizedToReadSpaces(String username) {
        return new UnauthorizedException("User " + username + " is not authorized to read spaces");
    }
}
