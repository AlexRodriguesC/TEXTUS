package ch.supsi.documentapplication.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RawModel(
        @NotNull String id,
        @NotNull String name,
        String description,
        @NotNull Architecture architecture,
        @NotNull List<Provider> providers,
        @NotNull Pricing pricing,
        @JsonProperty("context_length") Integer contextLength,
        @JsonProperty("json_output") Boolean jsonOutput,
        @JsonProperty("structured_outputs") Boolean structuredOutputs,
        Boolean free,
        String stability) {}
