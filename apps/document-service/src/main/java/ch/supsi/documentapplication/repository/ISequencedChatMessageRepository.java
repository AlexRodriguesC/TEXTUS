package ch.supsi.documentapplication.repository;

import ch.supsi.documentapplication.model.SequencedChatMessage;
import java.util.List;

public interface ISequencedChatMessageRepository {

    List<SequencedChatMessage> findByConversationId(String conversationId);
}
