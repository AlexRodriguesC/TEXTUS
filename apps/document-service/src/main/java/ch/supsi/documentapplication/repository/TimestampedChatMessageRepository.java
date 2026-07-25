package ch.supsi.documentapplication.repository;

import ch.supsi.documentapplication.dataaccess.IDatabaseChatMemoryStorage;
import ch.supsi.documentapplication.model.TimestampedChatMessage;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Repository;

@Repository
public class TimestampedChatMessageRepository implements ITimestampedChatMessageRepository {

    private final ChatMemory chatMemory;
    private final IDatabaseChatMemoryStorage chatMemoryStorage;

    public TimestampedChatMessageRepository(ChatMemory chatMemory, IDatabaseChatMemoryStorage chatMemoryStorage) {
        this.chatMemory = chatMemory;
        this.chatMemoryStorage = chatMemoryStorage;
    }

    @Override
    public List<TimestampedChatMessage> findByConversationId(String conversationId) {
        List<Message> messages = chatMemory.get(conversationId);
        List<Instant> timestamps = chatMemoryStorage.findTimestampsByConversationId(conversationId);

        if (messages.size() != timestamps.size()) {
            throw new IllegalStateException(String.format(
                    "Message count (%d) does not match timestamp count (%d) for conversation %s",
                    messages.size(), timestamps.size(), conversationId));
        }

        return IntStream.range(0, messages.size())
                .mapToObj(i -> new TimestampedChatMessage(messages.get(i), timestamps.get(i)))
                .toList();
    }
}
