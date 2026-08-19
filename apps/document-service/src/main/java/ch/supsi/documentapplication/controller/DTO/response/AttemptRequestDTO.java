package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.learn.ExerciseType;
import ch.supsi.documentapplication.model.learn.exercise.Question;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import java.util.List;

@Valid
@Schema(description = "Request body for submitting an attempt at the lesson's current exercise")
public record AttemptRequestDTO(
        ExerciseType exerciseType,
        @Schema(description = "Client-generated unique identifier for this attempt, used for idempotent retries", example = "att_5541")
        String attemptId,
        @Schema(description = "Identifier of the exercise being attempted; must match the lesson's current exercise")
        Long exerciseId,
        @Schema(description = "User's submitted response")
        Object response
) {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public <T> T getResponsesAs(Class<T> targetClass) {
        return MAPPER.convertValue(response, targetClass);
    }

    public <T> T getResponsesAs(TypeReference<T> typeReference) {
        return MAPPER.convertValue(response, typeReference);
    }
}
