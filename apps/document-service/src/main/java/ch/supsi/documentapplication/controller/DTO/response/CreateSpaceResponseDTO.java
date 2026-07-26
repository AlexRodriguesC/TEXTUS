package ch.supsi.documentapplication.controller.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;


@Valid
@Schema(description = "Response DTO containing the identifier of the newly created space")
public record CreateSpaceResponseDTO(
        @NotNull
        @Schema(description = "Unique identifier of the created space", example = "42")
        Long id
) {}
