package ch.supsi.documentapplication.controller.DTO.response;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

@Valid
public record ErrorResponseDTO(@NotNull String message) {}
