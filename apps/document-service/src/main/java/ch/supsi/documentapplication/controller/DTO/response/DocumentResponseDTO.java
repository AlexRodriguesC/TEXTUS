package ch.supsi.documentapplication.controller.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Response DTO containing details of a document")
public record DocumentResponseDTO(
        @NotNull @NotEmpty
                @Schema(description = "Name of the document", example = "meeting_notes.pdf")
                String name,

        @NotNull
                @Schema(description = "Unique identifier of the document", example = "101")
                Long id,

        @NotNull
                @Schema(description = "Pipeline processing tasks with per-stage progress")
                List<TaskDto> tasks) {}
