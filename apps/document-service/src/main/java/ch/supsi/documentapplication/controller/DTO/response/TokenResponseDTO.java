package ch.supsi.documentapplication.controller.DTO.response;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record TokenResponseDTO(@NotNull @NotEmpty String token) {}
