package ch.supsi.documentapplication.controller.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

@Valid
@Schema(description = "Request body for updating an existing space. All fields are optional.")
public record UpdateSpaceRequestDTO(
        @Schema(
                description = "New icon for the space",
                example = "1F603",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String icon,

        @Schema(
                description = "New display name for the space",
                example = "Updated Space Name",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String name,

        @Schema(
                description = "New description for the space",
                example = "An updated description for the space",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String description
) {}
