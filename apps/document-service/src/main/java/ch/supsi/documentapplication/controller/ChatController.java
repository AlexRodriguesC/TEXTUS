package ch.supsi.documentapplication.controller;

import ch.supsi.documentapplication.controller.DTO.request.CreateChatRequestDTO;
import ch.supsi.documentapplication.controller.DTO.request.SendMessageRequestDTO;
import ch.supsi.documentapplication.controller.DTO.response.*;
import ch.supsi.documentapplication.model.Chat;
import ch.supsi.documentapplication.service.IChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("api/v1/spaces/{spaceId}/chats")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Chats", description = "Endpoints for space-scoped chat conversations with streaming responses")
public class ChatController {

    private final IChatService chatService;

    @Autowired
    public ChatController(IChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List chats", description = "Returns all chat conversations for the current user in the space.")
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "200", description = "Chats retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Space not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    public ResponseEntity<ChatsResponseDTO> listChats(
            @PathVariable Long spaceId, @Parameter(hidden = true) Authentication authentication) {
        String username = authentication.getName();
        var chats = chatService.listChats(username, spaceId).stream().map(ChatResponseDTO::from).toList();
        return ResponseEntity.ok(new ChatsResponseDTO(chats));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create chat", description = "Creates a new chat conversation in the space.")
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "201", description = "Chat created successfully"),
                    @ApiResponse(responseCode = "404", description = "Space not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    public ResponseEntity<ChatResponseDTO> createChat(
            @PathVariable Long spaceId,
            @Valid @RequestBody(required = false) CreateChatRequestDTO request,
            @Parameter(hidden = true) Authentication authentication) {
        String username = authentication.getName();
        String title = request != null ? request.title() : null;
        Chat chat = chatService.createChat(username, spaceId, title);
        return ResponseEntity.status(HttpStatus.CREATED).body(ChatResponseDTO.from(chat));
    }

    @GetMapping(value = "/{chatId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get chat history", description = "Returns the message history for a chat conversation.")
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "200", description = "History retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Space or chat not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    public ResponseEntity<ChatHistoryResponseDTO> getHistory(
            @PathVariable Long spaceId,
            @PathVariable UUID chatId,
            @Parameter(hidden = true) Authentication authentication) {
        String username = authentication.getName();
        var messages = chatService.getHistory(username, spaceId, chatId);
        return ResponseEntity.ok(new ChatHistoryResponseDTO(messages));
    }

    @DeleteMapping("/{chatId}")
    @Operation(summary = "Delete chat", description = "Deletes a chat conversation and its message history.")
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "204", description = "Chat deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Space or chat not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    public ResponseEntity<Void> deleteChat(
            @PathVariable Long spaceId,
            @PathVariable UUID chatId,
            @Parameter(hidden = true) Authentication authentication) {
        String username = authentication.getName();
        chatService.deleteChat(username, spaceId, chatId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{chatId}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Send message", description = "Sends a user message and streams the assistant response as server-sent events.")
    @ApiResponses(
            {
                    @ApiResponse(responseCode = "200", description = "Streaming response started"),
                    @ApiResponse(responseCode = "404", description = "Space or chat not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDTO.class)))
            })
    public SseEmitter sendMessage(
            @PathVariable Long spaceId,
            @PathVariable UUID chatId,
            @Valid @RequestBody SendMessageRequestDTO request,
            @Parameter(hidden = true) Authentication authentication,
            HttpServletResponse response) {

        configureSseHeaders(response);

        Flux<String> messageStream = streamChatMessages(authentication, spaceId, chatId, request);

        return createSseEmitterForStream(messageStream);
    }

    private void configureSseHeaders(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-transform");
        response.setHeader(HttpHeaders.CONNECTION, "keep-alive");
    }

    private Flux<String> streamChatMessages(
            Authentication authentication, Long spaceId, UUID chatId, SendMessageRequestDTO request) {
        String username = authentication.getName();
        return chatService.streamMessage(
                username, spaceId, chatId, request.message(), request.model(), request.topicIds());
    }

    private SseEmitter createSseEmitterForStream(Flux<String> flux) {
        SseEmitter emitter = new SseEmitter(0L);
        Disposable subscription = subscribeToStream(flux, emitter);
        attachEmitterLifecycleHandlers(emitter, subscription);
        return emitter;
    }

    private Disposable subscribeToStream(Flux<String> flux, SseEmitter emitter) {
        return flux.subscribe(
                chunk -> sendChunkToEmitter(emitter, chunk),
                emitter::completeWithError,
                emitter::complete);
    }

    private void sendChunkToEmitter(SseEmitter emitter, String chunk) {
        try {
            emitter.send(SseEmitter.event().data(chunk));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    private void attachEmitterLifecycleHandlers(SseEmitter emitter, Disposable subscription) {
        emitter.onCompletion(subscription::dispose);
        emitter.onTimeout(() -> {
            subscription.dispose();
            emitter.complete();
        });
        emitter.onError(error -> {
            subscription.dispose();
            emitter.completeWithError(error);
        });
    }
}
