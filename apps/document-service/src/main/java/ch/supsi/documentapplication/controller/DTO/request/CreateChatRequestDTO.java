package ch.supsi.documentapplication.controller.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

@Valid
@Schema(description = "Request body for creating a new chat conversation")
public record CreateChatRequestDTO(
        @Schema(description = "Optional title for the conversation", example = "Summarize chapter 3") String title) {}
