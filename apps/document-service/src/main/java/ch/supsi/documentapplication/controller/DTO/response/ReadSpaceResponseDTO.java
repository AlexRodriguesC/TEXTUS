package ch.supsi.documentapplication.controller.DTO.response;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Valid
@Schema(description = "Response DTO containing the details of a space")
public record ReadSpaceResponseDTO(
        @NotNull
        @Schema(description = "Unique identifier of the space", example = "1")
        Long spaceId,

        @NotNull
        @Schema(description = "Icon identifier for the space", example = "1F600")
        String icon,

        @NotNull
        @Schema(description = "Name of the space", example = "My Project")
        String name,

        @NotNull
        @Schema(description = "Description of the space", example = "This space contains all project documents.")
        String description
) {}