package ch.supsi.documentapplication.controller.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Response DTO containing a list of topics")
public record TopicsResponseDTO(
        @NotNull
        @Schema(description = "List of topics")
        List<TopicResponseDTO> topics
) {}
