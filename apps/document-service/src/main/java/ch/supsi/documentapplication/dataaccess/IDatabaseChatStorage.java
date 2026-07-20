package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.Chat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IDatabaseChatStorage {

    UUID create(Long spaceId, String owner, String title);

    List<Chat> findBySpaceAndOwner(Long spaceId, String owner);

    Optional<Chat> findById(UUID chatId, Long spaceId, String owner);

    void updateTitle(UUID chatId, String title);

    void touchUpdatedAt(UUID chatId);

    void delete(UUID chatId);
}
