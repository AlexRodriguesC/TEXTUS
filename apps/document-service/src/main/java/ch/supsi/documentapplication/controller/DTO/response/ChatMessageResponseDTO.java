package ch.supsi.documentapplication.controller.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

@Schema(description = "A single message in a chat conversation")
public record ChatMessageResponseDTO(
        @Schema(description = "Role of the message author", example = "user") @NotNull String role,
        @Schema(description = "Message content") @NotNull String content,
        @Schema(description = "When the message was created") @NotNull Instant timestamp,
        @Schema(description = "Topic IDs the message is grounded on; empty when none") @NotNull List<Long> topics) { }
