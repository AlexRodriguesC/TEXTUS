package ch.supsi.documentapplication.controller.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents a single exercise served to the user for a lesson")
public record ExerciseResponseDTO(
        @Schema(description = "Unique identifier of the exercise") Long exerciseId,
        @Schema(description = "Identifier of the node this exercise belongs to") Long nodeId,
        @Schema(description = "Exercise family type", example = "pair") String family,
        @Schema(description = "Family-specific exercise content payload") Object content,
        @Schema(description = "Whether this exercise has already been attempted", example = "false") boolean completed
) {}
