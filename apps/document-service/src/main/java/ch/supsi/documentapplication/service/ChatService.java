package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.controller.DTO.response.ChatMessageResponseDTO;
import ch.supsi.documentapplication.dataaccess.IDatabaseChatMessageTopicStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseChatStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage;
import ch.supsi.documentapplication.exception.NotFoundException;
import ch.supsi.documentapplication.model.Chat;
import ch.supsi.documentapplication.model.SequencedChatMessage;
import ch.supsi.documentapplication.model.Space;
import ch.supsi.documentapplication.repository.ISequencedChatMessageRepository;
import ch.supsi.documentapplication.service.rag.ChatRagAdvisorFactory;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
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

    private final IDatabaseChatStorage chatStorage;
    private final IDatabaseChatMessageTopicStorage chatMessageTopicStorage;
    private final ISequencedChatMessageRepository sequencedChatMessageRepository;
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final ChatRagAdvisorFactory chatRagAdvisorFactory;
    private final String defaultModel;

    public ChatService(
            IDatabaseSpaceStorage spaceStorage,
            IDatabaseChatStorage chatStorage,
            IDatabaseChatMessageTopicStorage chatMessageTopicStorage,
            ISequencedChatMessageRepository sequencedChatMessageRepository,
            @Qualifier("interactiveChatClient") ChatClient chatClient,
            ChatMemory chatMemory,
            ChatRagAdvisorFactory chatRagAdvisorFactory,
            @Value("${spring.ai.openai.chat.model}") String defaultModel) {
        super(spaceStorage);
        this.chatStorage = chatStorage;
        this.chatMessageTopicStorage = chatMessageTopicStorage;
        this.sequencedChatMessageRepository = sequencedChatMessageRepository;
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
        Map<Long, List<Long>> topicsBySequenceId = chatMessageTopicStorage.findByChatId(chatId);
        return mapMessages(sequencedChatMessageRepository.findByConversationId(chatId.toString()), topicsBySequenceId);
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
        var stringChatId = chat.id().toString();

        List<Message> history = chatMemory.get(stringChatId);

        Advisor ragAdvisor = chatRagAdvisorFactory.createForSpace(space.getId());

        var promptSpec = chatClient
                .prompt()
                .messages(history)
                .user(message)
                .advisors(ragAdvisor)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, stringChatId));

        String resolvedModel = StringUtils.hasText(model) ? model : defaultModel;
        promptSpec = promptSpec.options(OpenAiChatOptions.builder().model(resolvedModel));

        Flux<ChatClientResponse> responses = promptSpec.stream().chatClientResponse();
        AtomicReference<List<Long>> topicIdsRef = new AtomicReference<>(List.of());

        return responses
                .doOnNext(response -> {
                    if (topicIdsRef.get().isEmpty()) {
                        List<Long> ids = extractTopicIds(response);
                        if (!ids.isEmpty()) {
                            topicIdsRef.set(ids);
                        }
                    }
                })
                .doOnComplete(() -> {
                    persistGrounding(chatId, topicIdsRef.get());
                    chatStorage.touchUpdatedAt(chatId);
                })
                .mapNotNull(this::extractStreamChunk);
    }

    private List<Long> extractTopicIds(ChatClientResponse response) {
        List<Document> documents = extractRetrievedDocuments(response);
        return documents.stream()
                .map(doc -> Long.valueOf(doc.getMetadata().get("topicId").toString()))
                .distinct()
                .toList();
    }

    @SuppressWarnings("unchecked")
    private List<Document> extractRetrievedDocuments(ChatClientResponse response) {
        Object raw = response.context().get(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT);
        return raw != null ? (List<Document>) raw : List.of();
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
        sequencedChatMessageRepository.findByConversationId(chatId.toString()).stream()
                .filter(wrapper -> wrapper.message().getMessageType() == MessageType.ASSISTANT)
                .max(Comparator.comparingLong(SequencedChatMessage::sequenceId))
                .ifPresent(wrapper -> chatMessageTopicStorage.save(chatId, wrapper.sequenceId(), topicIds));
    }

    private Chat getChatOrThrow(String username, Long spaceId, UUID chatId) {
        getSpaceOrThrow(username, spaceId);
        return chatStorage
                .findById(chatId, spaceId, username)
                .orElseThrow(() -> NotFoundException.chatNotFound(chatId));
    }

    private List<ChatMessageResponseDTO> mapMessages(
            List<SequencedChatMessage> messages, Map<Long, List<Long>> topicsBySequenceId) {
        return messages.stream()
                .filter(wrapper -> wrapper.message().getMessageType() != MessageType.SYSTEM)
                .map(wrapper -> {
                    String role = wrapper.message().getMessageType().getValue().toLowerCase(Locale.ROOT);
                    List<Long> topics = MessageType.ASSISTANT
                                    .name()
                                    .toLowerCase(Locale.ROOT)
                                    .equals(role)
                            ? topicsBySequenceId.getOrDefault(wrapper.sequenceId(), List.of())
                            : List.of();
                    return new ChatMessageResponseDTO(role, wrapper.message().getText(), wrapper.timestamp(), topics);
                })
                .toList();
    }
}
