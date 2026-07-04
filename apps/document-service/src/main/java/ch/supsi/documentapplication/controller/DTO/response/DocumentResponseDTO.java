package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.DocumentStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record DocumentResponseDTO(@NotNull @NotEmpty String name, @NotNull Long id, @NotNull DocumentStatus status) {}
