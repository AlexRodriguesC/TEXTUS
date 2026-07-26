package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.MindMapStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

@Valid
@Schema(description = "Summary of a mind map including its generation status")
public record MindMapSummaryDTO(
        @NotNull @Schema(description = "Unique identifier of the mind map", example = "1") Long id,
        @NotNull @Schema(description = "Current status of the mind map generation", example = "GENERATING")
                MindMapStatus status) {}
