package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.learn.LearningPath;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Response DTO containing the full learning path with settings and chapters")
public record LearningPathResponseDTO(
        @NotNull
        @Schema(description = "Settings of the learning path")
        PathSettingsResponseDTO settings,

        @NotNull
        @Schema(description = "List of chapters in the learning path")
        List<ChapterResponseDTO> chapters
) {

    public static LearningPathResponseDTO from(LearningPath learningPath) {
        PathSettingsResponseDTO settings = PathSettingsResponseDTO.from(learningPath.getSettings());
        List<ChapterResponseDTO> chapters = learningPath.getChapters().stream().map(ChapterResponseDTO::from).toList();
        return new LearningPathResponseDTO(settings, chapters);
    }
}
