package ch.supsi.documentapplication.dataaccess;

import java.time.Instant;
import java.util.List;

public interface IDatabaseChatMemoryStorage {

    List<Instant> findTimestampsByConversationId(String conversationId);
}
