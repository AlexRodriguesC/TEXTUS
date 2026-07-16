package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.MindMapStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Response DTO containing the status of a mind map job")
public record MindMapJobDTO(
        @NotNull
        @Schema(description = "Current status of the mind map job", example = "DONE")
        MindMapStatus status
) {}
