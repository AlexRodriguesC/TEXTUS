package ch.supsi.documentapplication.repository;

import ch.supsi.documentapplication.model.TimestampedChatMessage;
import java.util.List;

public interface ITimestampedChatMessageRepository {

    List<TimestampedChatMessage> findByConversationId(String conversationId);
}
