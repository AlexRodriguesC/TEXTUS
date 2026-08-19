package ch.supsi.documentapplication.controller.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

@Valid
@Schema(description = "Response DTO containing an authentication token")
public record TokenResponseDTO(
        @NotNull @NotEmpty
        @Schema(description = "JWT token for authenticated sessions", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token
) {}