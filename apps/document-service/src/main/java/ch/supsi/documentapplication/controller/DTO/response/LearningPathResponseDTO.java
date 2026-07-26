package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.learn.LearningPath;
import ch.supsi.documentapplication.model.learn.LearningPathStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

import java.util.List;

@Valid
@Schema(description = "Response DTO containing the full learning path with settings, chapters, and generation status")
public record LearningPathResponseDTO(
        @NotNull
        @Schema(description = "Current status of the learning path generation", example = "DONE")
        LearningPathStatus status,

        @NotNull
        @Schema(description = "Settings of the learning path")
        PathSettingsResponseDTO settings,

        @NotNull
        @Schema(description = "List of chapters in the learning path")
        List<ChapterResponseDTO> chapters
) {

    public static LearningPathResponseDTO from(LearningPath learningPath, LearningPathStatus status) {
        PathSettingsResponseDTO settings = PathSettingsResponseDTO.from(learningPath.getSettings());
        List<ChapterResponseDTO> chapters = learningPath.getChapters().stream().map(ChapterResponseDTO::from).toList();
        return new LearningPathResponseDTO(status, settings, chapters);
    }
}
