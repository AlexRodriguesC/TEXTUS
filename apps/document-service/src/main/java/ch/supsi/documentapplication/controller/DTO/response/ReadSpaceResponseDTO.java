package ch.supsi.documentapplication.controller.DTO.response;

import jakarta.validation.constraints.NotNull;

public record ReadSpaceResponseDTO(@NotNull Long spaceId, @NotNull String name, @NotNull String description) {}
