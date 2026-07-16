package ch.supsi.documentapplication.controller.DTO.response;

import jakarta.validation.constraints.NotNull;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response DTO containing a list of documents")
public record DocumentsResponseDTO(
        @NotNull
        @Schema(description = "List of documents", example = "[{\"name\": \"file1.pdf\", \"id\": 1, \"status\": \"RECEIVED\"}]")
        List<DocumentResponseDTO> documents
) {}