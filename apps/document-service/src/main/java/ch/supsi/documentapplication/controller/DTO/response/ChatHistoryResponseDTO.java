package ch.supsi.documentapplication.controller.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

@Schema(description = "Message history for a chat conversation")
public record ChatHistoryResponseDTO(
        @Schema(description = "Messages in chronological order") @NotNull List<ChatMessageResponseDTO> messages) {}
