package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.learn.LearningPathStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Response DTO containing the status of a learning path job")
public record LearningPathJobDTO(
        @NotNull
        @Schema(description = "Current status of the learning path job", example = "DONE")
        LearningPathStatus status
) {}
