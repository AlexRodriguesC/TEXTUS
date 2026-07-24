package ch.supsi.documentapplication.dataaccess;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IDatabaseChatMessageTopicStorage {

    void save(UUID chatId, Instant messageTimestamp, List<Long> topicIds);

    Map<Instant, List<Long>> findByChatId(UUID chatId);
}
