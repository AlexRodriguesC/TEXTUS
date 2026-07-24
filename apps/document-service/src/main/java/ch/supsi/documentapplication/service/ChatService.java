package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.controller.DTO.response.ChatMessageResponseDTO;
import ch.supsi.documentapplication.dataaccess.IDatabaseChatStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage;
import ch.supsi.documentapplication.exception.NotFoundException;
import ch.supsi.documentapplication.model.Chat;
import ch.supsi.documentapplication.model.Space;
import ch.supsi.documentapplication.service.rag.ChatRagAdvisorFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

@Service
public class ChatService extends ch.supsi.documentapplication.service.Service implements IChatService {

    private static final int AUTO_TITLE_MAX_LENGTH = 50;
    private static final String MESSAGE_TIMESTAMP_METADATA = "timestamp";

    private final IDatabaseChatStorage chatStorage;
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final ChatRagAdvisorFactory chatRagAdvisorFactory;
    private final String defaultModel;

    public ChatService(
            IDatabaseSpaceStorage spaceStorage,
            IDatabaseChatStorage chatStorage,
            @Qualifier("interactiveChatClient") ChatClient chatClient,
            ChatMemory chatMemory,
            ChatRagAdvisorFactory chatRagAdvisorFactory,
            @Value("${spring.ai.openai.chat.model}") String defaultModel) {
        super(spaceStorage);
        this.chatStorage = chatStorage;
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.chatRagAdvisorFactory = chatRagAdvisorFactory;
        this.defaultModel = defaultModel;
    }

    @Override
    public List<Chat> listChats(String username, Long spaceId) {
        getSpaceOrThrow(username, spaceId);
        return chatStorage.findBySpaceAndOwner(spaceId, username);
    }

    @Override
    public Chat createChat(String username, Long spaceId, String title) {
        getSpaceOrThrow(username, spaceId);
        String resolvedTitle = StringUtils.hasText(title) ? title.trim() : Chat.DEFAULT_TITLE;
        UUID chatId = chatStorage.create(spaceId, username, resolvedTitle);
        return chatStorage
                .findById(chatId, spaceId, username)
                .orElseThrow(() -> NotFoundException.chatNotFound(chatId));
    }

    @Override
    public List<ChatMessageResponseDTO> getHistory(String username, Long spaceId, UUID chatId) {
        getChatOrThrow(username, spaceId, chatId);
        return mapMessages(chatMemory.get(chatId.toString()));
    }

    @Override
    public void deleteChat(String username, Long spaceId, UUID chatId) {
        getChatOrThrow(username, spaceId, chatId);
        chatMemory.clear(chatId.toString());
        chatStorage.delete(chatId);
    }

    @Override
    public Flux<String> streamMessage(String username, Long spaceId, UUID chatId, String message, String model) {
        Space space = getSpaceOrThrow(username, spaceId);
        Chat chat = getChatOrThrow(username, spaceId, chatId);
        maybeAutoTitle(chat, message);

        List<Message> history = chatMemory.get(chatId.toString()).stream()
                .filter(msg -> !(msg instanceof SystemMessage))
                .toList();

        Advisor ragAdvisor = chatRagAdvisorFactory.createForSpace(space.getId());

        var promptSpec = chatClient
                .prompt()
                .messages(history)
                .user(message)
                .advisors(ragAdvisor)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId.toString()));

        String resolvedModel = StringUtils.hasText(model) ? model : defaultModel;
        promptSpec = promptSpec.options(OpenAiChatOptions.builder().model(resolvedModel));

        return promptSpec.stream().content().doOnComplete(() -> chatStorage.touchUpdatedAt(chatId));
    }

    private Chat getChatOrThrow(String username, Long spaceId, UUID chatId) {
        getSpaceOrThrow(username, spaceId);
        return chatStorage
                .findById(chatId, spaceId, username)
                .orElseThrow(() -> NotFoundException.chatNotFound(chatId));
    }

    private void maybeAutoTitle(Chat chat, String message) {
        if (!Chat.DEFAULT_TITLE.equals(chat.title())) {
            return;
        }
        String trimmed = message.trim();
        if (!StringUtils.hasText(trimmed)) {
            return;
        }
        String title = trimmed.length() <= AUTO_TITLE_MAX_LENGTH
                ? trimmed
                : trimmed.substring(0, AUTO_TITLE_MAX_LENGTH - 1) + "…";
        chatStorage.updateTitle(chat.id(), title);
    }

    private List<ChatMessageResponseDTO> mapMessages(List<Message> messages) {
        List<ChatMessageResponseDTO> result = new ArrayList<>();
        for (Message message : messages) {
            if (message instanceof SystemMessage) {
                continue;
            }
            String role = mapRole(message);
            if (role == null) {
                continue;
            }
            Instant timestamp = extractTimestamp(message);
            result.add(new ChatMessageResponseDTO(role, message.getText(), timestamp));
        }
        return result;
    }

    private String mapRole(Message message) {
        MessageType type = message.getMessageType();
        return switch (type) {
            case USER -> "user";
            case ASSISTANT -> "assistant";
            case SYSTEM -> "system";
            default -> null;
        };
    }

    private Instant extractTimestamp(Message message) {
        Map<String, Object> metadata = message.getMetadata();
        for (String key : List.of(MESSAGE_TIMESTAMP_METADATA, "conversation_ts", "jdbc.conversation_ts")) {
            Object timestamp = metadata.get(key);
            if (timestamp instanceof Instant instant) {
                return instant;
            }
        }
        return Instant.now();
    }
}
