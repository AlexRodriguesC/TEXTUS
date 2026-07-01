package ch.supsi.documentapplication.controller.DTO.response;

import jakarta.validation.constraints.NotNull;

public record ErrorResponseDTO(@NotNull String message) {}
