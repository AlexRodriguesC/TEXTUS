package ch.supsi.documentapplication.controller.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Response DTO containing a topic with its details")
public record TopicResponseDTO(
        @NotNull
        @Schema(description = "Unique identifier of the topic", example = "1")
        Long id,

        @NotNull @NotEmpty
        @Schema(description = "Title of the topic", example = "Introduction to Java")
        String title,

        @NotNull @NotEmpty
        @Schema(description = "Summary of the topic", example = "This topic covers basic Java syntax and concepts.")
        String summary,

        @NotNull @NotEmpty
        @Schema(description = "List of labels associated with the topic", example = "[\"programming\", \"java\", \"basics\"]")
        List<@NotNull @NotEmpty String> labels
) {}
