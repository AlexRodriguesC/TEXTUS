package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.controller.DTO.response.ChatMessageResponseDTO;
import ch.supsi.documentapplication.model.Chat;
import java.util.List;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface IChatService {

    List<Chat> listChats(String username, Long spaceId);

    Chat createChat(String username, Long spaceId, String title);

    List<ChatMessageResponseDTO> getHistory(String username, Long spaceId, UUID chatId);

    void deleteChat(String username, Long spaceId, UUID chatId);

    Flux<String> streamMessage(
            String username, Long spaceId, UUID chatId, String message, String model, List<Long> topicIds);
}
