package ch.supsi.documentapplication.service.clients;

public class LLMInputSanitizer implements StringSanitizer {

    @Override
    public String sanitize(String toSanitize) {
        if (toSanitize == null) return null;
        return toSanitize
                .strip() // trim leading/trailing whitespace
                .replaceAll("(?s)^```json\\s*", "") // strip opening ```json
                .replaceAll("(?s)^```\\s*", "") // strip opening ``` (no language tag)
                .replaceAll("(?s)```\\s*$", "") // strip closing ```
                .strip();
    }
}
