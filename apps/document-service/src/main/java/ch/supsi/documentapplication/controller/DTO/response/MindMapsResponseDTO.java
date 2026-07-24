package ch.supsi.documentapplication.controller.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "List of mind maps in a space")
public record MindMapsResponseDTO(
        @Schema(description = "Mind maps associated with the space") @NotNull List<MindMapSummaryDTO> mindMaps) {}
