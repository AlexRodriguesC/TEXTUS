package ch.supsi.documentapplication.controller.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Valid
@Schema(description = "Request body for creating a new space")
public class CreateSpaceRequestDTO {

    @NotNull
    @NotEmpty
    @Schema(
            description = "Icon representing the space",
            example = "1F600",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String icon;

    @NotNull
    @NotEmpty
    @Schema(
            description = "Display name of the space",
            example = "My First Space",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @NotNull
    @NotEmpty
    @Schema(
            description = "Short description of the space",
            example = "A space for my personal documents",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String description;
}