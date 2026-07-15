package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.learn.LearningPathStatus;
import jakarta.validation.constraints.NotNull;

public record LearningPathJobDTO(@NotNull LearningPathStatus status) {
}
