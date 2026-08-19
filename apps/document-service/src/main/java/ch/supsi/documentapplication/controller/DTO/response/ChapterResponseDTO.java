package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.learn.Chapter;
import ch.supsi.documentapplication.model.learn.Lesson;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

@Valid
@Schema(description = "Represents a chapter within a learning path, including its position, title, description, and the nodes (collections of topics) contained in it.")
public record   ChapterResponseDTO(
        @NotNull
        @Schema(
                description = "Relative position of the chapter inside the path (0-based index, e.g., 0 for first, 1 for second)",
                example = "0",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Integer index,

        @NotNull
        @Schema(
                description = "Title of the chapter",
                example = "Introduction to Variables",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String title,

        @NotNull
        @Schema(
                description = "Short description of the chapter's content",
                example = "Covers basic variable types and declarations",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String description,

        @NotNull
        @Schema(
                description = "List of lessons (collections of topics) that make up the chapter",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        List<LessonResponseDTO> lessons
)
{

    public static ChapterResponseDTO from(Chapter chapter) {
        var nodes = chapter.getLessons();
        var nodeDTOs = new ArrayList<LessonResponseDTO>();
        for (Lesson lesson : nodes) {
            LessonResponseDTO dto = LessonResponseDTO.from(lesson);
            nodeDTOs.add(dto);
        }

        return new ChapterResponseDTO(chapter.getIndex(), chapter.getTitle(), chapter.getDescription(), nodeDTOs);
    }
}
