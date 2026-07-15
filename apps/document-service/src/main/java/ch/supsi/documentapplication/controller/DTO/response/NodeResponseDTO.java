package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.learn.NodeType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;

import java.util.List;

public record NodeResponseDTO(
        @NotNull Long id,
        @NotNull NodeType type,
        @NotNull List<Long> topics
) {
}
