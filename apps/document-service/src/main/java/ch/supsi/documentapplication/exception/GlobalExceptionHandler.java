package ch.supsi.documentapplication.exception;

import ch.supsi.documentapplication.controller.DTO.response.ErrorResponseDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ProblemDetail handle(ApplicationException e) {
        return ProblemDetail.forStatusAndDetail(e.getHttpStatus(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handle(Exception e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(UsernameTakenException.class)
    public ResponseEntity<ErrorResponseDTO> usernameTakenException(UsernameTakenException e) {
        return new ResponseEntityBuilder()
                .status(HttpStatus.CONFLICT)
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> notFoundException(NotFoundException e) {
        return new ResponseEntityBuilder()
                .status(HttpStatus.NOT_FOUND)
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponseDTO> unauthorizedException(UnauthorizedException e) {
        return new ResponseEntityBuilder()
                .status(HttpStatus.UNAUTHORIZED)
                .message(e.getMessage())
                .build();
    }

    static class ResponseEntityBuilder {

        private String message;
        private HttpStatus status;

        ResponseEntityBuilder() {}

        ResponseEntityBuilder message(@NotNull String message) {
            this.message = message;
            return this;
        }

        ResponseEntityBuilder status(@NotNull HttpStatus status) {
            this.status = status;
            return this;
        }

        ResponseEntity<ErrorResponseDTO> build() {
            return ResponseEntity.status(status).body(new ErrorResponseDTO(message));
        }
    }
}
