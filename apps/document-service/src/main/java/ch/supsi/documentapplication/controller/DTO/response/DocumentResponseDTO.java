package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.DocumentStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response DTO containing details of a document")
public record DocumentResponseDTO(
        @NotNull @NotEmpty
        @Schema(description = "Name of the document", example = "meeting_notes.pdf")
        String name,

        @NotNull
        @Schema(description = "Unique identifier of the document", example = "101")
        Long id,

        @NotNull
        @Schema(description = "Current status of the document", example = "RECEIVED")
        DocumentStatus status
) {}