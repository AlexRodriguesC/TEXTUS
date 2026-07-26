package ch.supsi.documentapplication.controller.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Valid
@Schema(description = "Request body for sending a chat message")
public record SendMessageRequestDTO(
        @Schema(description = "The user message to send", example = "Summarize the main findings") @NotBlank String message,
        @Schema(description = "Model identifier to use for this request", example = "google-ai-studio/gemini-2.5-flash")
                String model) {}
