package ch.supsi.textusbackend.exception;

public class PromptTemplateLoadException extends BookDomainException {
    public PromptTemplateLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
