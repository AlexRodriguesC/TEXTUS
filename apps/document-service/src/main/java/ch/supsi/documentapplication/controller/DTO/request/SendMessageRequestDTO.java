package ch.supsi.documentapplication.controller.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Valid
@Schema(description = "Request body for sending a chat message")
public record SendMessageRequestDTO(
        @Schema(description = "The user message to send", example = "Summarize the main findings") @NotBlank String message,
        @Schema(description = "Model identifier to use for this request", example = "google-ai-studio/gemini-2.5-flash")
                String model,
        @Schema(
                        description =
                                "Optional topic IDs to pin as primary RAG context for this message only. Invalid or foreign IDs are ignored.",
                        example = "[12, 45]")
                @Size(max = 10)
                List<Long> topicIds) {}
