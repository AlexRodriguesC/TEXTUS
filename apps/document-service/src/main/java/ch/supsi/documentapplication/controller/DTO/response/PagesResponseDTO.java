package ch.supsi.documentapplication.controller.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Response DTO containing all pages in a space")
public record PagesResponseDTO(
        @NotNull @Schema(description = "List of pages in the space") List<@NotNull PageResponseDTO> pages) {}
