package ch.supsi.documentapplication.model.learn.exercise;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@JsonClassDescription("One matched pair: a left element and its correct right counterpart.")
public record LeftRight(
        @JsonPropertyDescription("The left-side element of the pair, grounded in TOPIC.") @NotNull @NotEmpty
                String left,
        @JsonPropertyDescription("The right-side element that correctly matches the left element, grounded in TOPIC.")
                @NotNull
                @NotEmpty
                String right) {}
