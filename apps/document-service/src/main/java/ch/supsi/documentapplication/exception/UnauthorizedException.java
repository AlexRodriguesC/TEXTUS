package ch.supsi.documentapplication.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApplicationException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }

    public static UnauthorizedException userNotOwnerOfSpace(String username, Long spaceId) {
        return new UnauthorizedException(format("User %s is not the owner of space with id %d", username, spaceId));
    }

    public static UnauthorizedException userNotAuthorizedToReadSpaces(String username) {
        return new UnauthorizedException(format("User %s is not authorized to read spaces", username));
    }
}
