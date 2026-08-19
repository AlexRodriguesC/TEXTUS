package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.TaskType;
import ch.supsi.documentapplication.model.TaskVariant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

@Valid
@Schema(description = "Processing task progress for a document pipeline stage")
public record TaskDto(
        @NotNull
                @Schema(description = "Stable task identifier", example = "42:OCR")
                String id,
        @NotNull @Schema(description = "Pipeline stage type", example = "OCR") TaskType type,
        @NotNull @Schema(description = "Completed work units", example = "3") int progress,
        @NotNull @Schema(description = "Total work units for this stage", example = "10") int maxProgress,
        @NotNull @Schema(description = "Task outcome variant", example = "DEFAULT") TaskVariant variant) {}
