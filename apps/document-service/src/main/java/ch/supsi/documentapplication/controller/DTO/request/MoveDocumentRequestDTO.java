package ch.supsi.documentapplication.controller.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for changing a document's position within its space")
public record MoveDocumentRequestDTO(
        @NotNull
                @Min(0)
                @Schema(description = "Requested zero-based document position", example = "0")
                Integer orderIndex) {}
