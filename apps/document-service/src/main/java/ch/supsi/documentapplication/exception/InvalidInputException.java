package ch.supsi.documentapplication.exception;

import org.springframework.http.HttpStatus;

public class InvalidInputException extends ApplicationException {

    public InvalidInputException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public static InvalidInputException invalidSettings() {
        return new InvalidInputException("The provided settings are invalid.");
    }

    public static InvalidInputException attemptsNotExhausted() {
        return new InvalidInputException("Attempts for specified lesson not exhausted");
    }

    public static InvalidInputException invalidDocumentOrderIndex(int orderIndex, int documentCount) {
        return new InvalidInputException(
                format("Document order index %d must be between 0 and %d", orderIndex, documentCount - 1));
    }

    public static InvalidInputException invalidDocumentOrderIndex(int orderIndex) {
        return new InvalidInputException(format("Document order index %d is out of range", orderIndex));
    }
}
