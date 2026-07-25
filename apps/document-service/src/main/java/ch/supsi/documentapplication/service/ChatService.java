package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.controller.DTO.response.ChatMessageResponseDTO;
import ch.supsi.documentapplication.dataaccess.IDatabaseChatMessageTopicStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseChatStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage;
import ch.supsi.documentapplication.exception.NotFoundException;
import ch.supsi.documentapplication.model.Chat;
import ch.supsi.documentapplication.model.Space;
import ch.supsi.documentapplication.service.rag.ChatRagAdvisorFactory;
import ch.supsi.documentapplication.service.rag.TopicGroundingExtractor;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
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
    private final IDatabaseChatMessageTopicStorage chatMessageTopicStorage;
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final ChatRagAdvisorFactory chatRagAdvisorFactory;
    private final String defaultModel;

    public ChatService(
            IDatabaseSpaceStorage spaceStorage,
            IDatabaseChatStorage chatStorage,
            IDatabaseChatMessageTopicStorage chatMessageTopicStorage,
            @Qualifier("interactiveChatClient") ChatClient chatClient,
            ChatMemory chatMemory,
            ChatRagAdvisorFactory chatRagAdvisorFactory,
            @Value("${spring.ai.openai.chat.model}") String defaultModel) {
        super(spaceStorage);
        this.chatStorage = chatStorage;
        this.chatMessageTopicStorage = chatMessageTopicStorage;
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
        Map<Instant, List<Long>> topicsByTimestamp = chatMessageTopicStorage.findByChatId(chatId);
        return mapMessages(chatMemory.get(chatId.toString()), topicsByTimestamp);
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

        List<Message> history = chatMemory.get(chatId.toString());

        Advisor ragAdvisor = chatRagAdvisorFactory.createForSpace(space.getId());

        var promptSpec = chatClient
                .prompt()
                .messages(history)
                .user(message)
                .advisors(ragAdvisor)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId.toString()));

        String resolvedModel = StringUtils.hasText(model) ? model : defaultModel;
        promptSpec = promptSpec.options(OpenAiChatOptions.builder().model(resolvedModel));

        AtomicReference<List<Long>> topicIds = new AtomicReference<>(List.of());

        return promptSpec.stream()
                .chatClientResponse()
                .doOnNext(response -> captureTopicIds(response, topicIds))
                .mapNotNull(this::extractStreamChunk)
                .doOnComplete(() -> {
                    chatStorage.touchUpdatedAt(chatId);
                    persistGrounding(chatId, topicIds.get());
                });
    }

    private void captureTopicIds(ChatClientResponse response, AtomicReference<List<Long>> topicIds) {
        Object documentContext = response.context().get(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT);
        if (documentContext instanceof List<?> documents) {
            List<Document> typedDocuments = documents.stream()
                    .filter(Document.class::isInstance)
                    .map(Document.class::cast)
                    .toList();
            topicIds.set(TopicGroundingExtractor.extractTopicIds(typedDocuments));
        }
    }

    private String extractStreamChunk(ChatClientResponse response) {
        if (response.chatResponse() == null || response.chatResponse().getResult() == null) {
            return null;
        }
        return response.chatResponse().getResult().getOutput().getText();
    }

    private void persistGrounding(UUID chatId, List<Long> topicIds) {
        if (topicIds.isEmpty()) {
            return;
        }
        List<Message> messages = chatMemory.get(chatId.toString());
        for (int index = messages.size() - 1; index >= 0; index--) {
            Message message = messages.get(index);
            if (message instanceof SystemMessage) {
                continue;
            }
            if (message.getMessageType() == MessageType.ASSISTANT) {
                chatMessageTopicStorage.save(chatId, extractTimestamp(message), topicIds);
            }
            return;
        }
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

    private List<ChatMessageResponseDTO> mapMessages(
            List<Message> messages, Map<Instant, List<Long>> topicsByTimestamp) {
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
            List<Long> topics =
                    "assistant".equals(role) ? topicsByTimestamp.getOrDefault(timestamp, List.of()) : List.of();
            result.add(new ChatMessageResponseDTO(role, message.getText(), timestamp, topics));
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
