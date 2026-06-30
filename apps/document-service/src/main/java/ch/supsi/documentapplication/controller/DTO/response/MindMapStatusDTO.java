package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.MindMapStatus;
import jakarta.validation.constraints.NotNull;

public record MindMapStatusDTO(@NotNull MindMapStatus status) {}
