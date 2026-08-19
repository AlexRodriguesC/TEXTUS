package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.learn.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

import java.util.List;

@Valid
@Schema(description = "Response DTO containing a node in a learning path chapter")
public record NodeResponseDTO(
        @NotNull
        @Schema(description = "Unique identifier of the node", example = "1")
        Long id,

        @NotNull
        @Schema(description = "Type of the node (either LESSON or CHECKPOINT)", example = "LESSON")
        LessonType type,

        @NotNull
        @Schema(description = "List of topic IDs associated with this node", example = "[101, 102, 103]")
        List<@NotNull Long> topics
) {}
