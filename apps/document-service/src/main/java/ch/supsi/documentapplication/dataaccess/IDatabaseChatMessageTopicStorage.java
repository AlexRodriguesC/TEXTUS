package ch.supsi.documentapplication.dataaccess;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IDatabaseChatMessageTopicStorage {

    void save(UUID chatId, long messageSequenceId, List<Long> topicIds);

    Map<Long, List<Long>> findByChatId(UUID chatId);
}
