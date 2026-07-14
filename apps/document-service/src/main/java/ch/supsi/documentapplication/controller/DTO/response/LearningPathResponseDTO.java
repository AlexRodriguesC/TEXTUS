package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.Chapter;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record LearningPathResponseDTO(@NotNull List<Chapter> chapters) {
}
