package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.learn.LearningPath;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record LearningPathResponseDTO(@NotNull PathSettingsResponseDTO settings, @NotNull List<ChapterResponseDTO> chapters) {

    public static LearningPathResponseDTO from(LearningPath learningPath) {
        PathSettingsResponseDTO settings = PathSettingsResponseDTO.from(learningPath.getSettings());
        List<ChapterResponseDTO> chapters = learningPath.getChapters().stream().map(ChapterResponseDTO::from).toList();
        return new LearningPathResponseDTO(settings, chapters);
    }
}
