package ch.supsi.documentapplication.controller.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Response DTO containing a page with its details")
public record PageResponseDTO(
        @NotNull @Schema(description = "Unique identifier of the page", example = "1") Long id,

        @NotNull @Schema(description = "Identifier of the document this page belongs to", example = "1") Long documentId,

        @NotNull @NotEmpty @Schema(description = "Title of the page", example = "Introduction") String title,

        @NotNull @NotEmpty @Schema(description = "Summary of the page", example = "Overview of core concepts.") String summary) {}
