package ch.supsi.textusbackend.exception;

public class PromptTemplateLoadException extends DocumentDomainException {
    public PromptTemplateLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
