package ch.supsi.documentapplication.controller.DTO.response;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ChapterResponseDTO(@NotNull Integer index, @NotNull String title, @NotNull String description, @NotNull List<Long> topics) {
}
