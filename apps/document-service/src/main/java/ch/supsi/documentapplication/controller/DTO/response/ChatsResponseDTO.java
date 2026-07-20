package ch.supsi.documentapplication.controller.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "List of chat conversations in a space")
public record ChatsResponseDTO(@Schema(description = "Chat conversations") @NotNull List<ChatResponseDTO> chats) {}
