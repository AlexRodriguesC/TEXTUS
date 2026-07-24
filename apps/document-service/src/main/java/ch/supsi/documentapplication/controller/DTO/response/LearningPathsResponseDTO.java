package ch.supsi.documentapplication.controller.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "List of learning paths in a space")
public record LearningPathsResponseDTO(
        @Schema(description = "Learning paths associated with the space")
        @NotNull
        List<LearningPathResponseDTO> paths) {}
