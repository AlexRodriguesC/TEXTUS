package ch.supsi.documentapplication.controller.DTO.response;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ModelResponseDTO(
        @NotNull @NotEmpty String id,
        @NotNull @NotEmpty String name,
        @NotNull @NotEmpty String description,
        @NotNull String promptPrice,
        @NotNull String completionPrice,
        @NotNull Integer contextLength,
        @NotNull @NotEmpty String stability,
        boolean free
) {}
