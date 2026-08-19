package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.Chat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;

@Valid
@Schema(description = "Metadata for a chat conversation")
public record ChatResponseDTO(
        @Schema(description = "Unique identifier of the chat") @NotNull UUID id,
        @Schema(description = "Title of the conversation") @NotNull String title,
        @Schema(description = "When the chat was created") @NotNull Instant createdAt,
        @Schema(description = "When the chat was last updated") @NotNull Instant updatedAt) {

    public static ChatResponseDTO from(Chat chat) {
        return new ChatResponseDTO(chat.id(), chat.title(), chat.createdAt(), chat.updatedAt());
    }
}
