package ch.supsi.documentapplication.controller.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Feedback returned after submitting an attempt")
public record AttemptResponseDTO(
        @Schema(description = "Identifier of the attempted exercise") Long exerciseId,
        @Schema(description = "Whether the overall attempt was correct") boolean correct,
        @Schema(description = "Updated lesson state after this attempt, including new mastery and status") LessonResponseDTO lesson
) {}
