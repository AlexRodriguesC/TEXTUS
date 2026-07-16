package ch.supsi.documentapplication.controller.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Request body for submitting an attempt at the lesson's current exercise")
public record AttemptRequestDTO(
        @Schema(description = "Client-generated unique identifier for this attempt, used for idempotent retries", example = "att_5541") String attemptId,
        @Schema(description = "Identifier of the exercise being attempted; must match the lesson's current exercise") Long exerciseId,
        @Schema(description = "User's submitted responses, shape depends on exercise family") List<Object> responses
) {}
