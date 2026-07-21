package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.Chat;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class DatabaseChatStorage extends DatabaseStorage implements IDatabaseChatStorage {

    public static final String SPACE_ID = "spaceId";
    public static final String OWNER = "owner";
    public static final String TITLE = "title";
    public static final String CHAT_ID = "chatId";

    public DatabaseChatStorage(JdbcClient jdbcClient) {
        super(jdbcClient);
    }

    @Override
    public UUID create(Long spaceId, String owner, String title) {
        return jdbcClient
                .sql(
                        """
                        INSERT INTO documents.chat (space_id, owner_id, title)
                        VALUES (:spaceId, :owner, :title)
                        RETURNING id
                        """)
                .param(SPACE_ID, spaceId)
                .param(OWNER, owner)
                .param(TITLE, title)
                .query(UUID.class)
                .single();
    }

    @Override
    public List<Chat> findBySpaceAndOwner(Long spaceId, String owner) {
        return jdbcClient
                .sql(
                        """
                        SELECT id, space_id, owner_id, title, created_at, updated_at
                        FROM documents.chat
                        WHERE space_id = :spaceId AND owner_id = :owner
                        ORDER BY updated_at DESC
                        """)
                .param(SPACE_ID, spaceId)
                .param(OWNER, owner)
                .query(this::mapRow)
                .list();
    }

    @Override
    public Optional<Chat> findById(UUID chatId, Long spaceId, String owner) {
        return jdbcClient
                .sql(
                        """
                        SELECT id, space_id, owner_id, title, created_at, updated_at
                        FROM documents.chat
                        WHERE id = :chatId AND space_id = :spaceId AND owner_id = :owner
                        """)
                .param(CHAT_ID, chatId)
                .param(SPACE_ID, spaceId)
                .param(OWNER, owner)
                .query(this::mapRow)
                .optional();
    }

    @Override
    public void updateTitle(UUID chatId, String title) {
        jdbcClient
                .sql(
                        """
                        UPDATE documents.chat
                        SET title = :title, updated_at = NOW()
                        WHERE id = :chatId
                        """)
                .param(CHAT_ID, chatId)
                .param(TITLE, title)
                .update();
    }

    @Override
    public void touchUpdatedAt(UUID chatId) {
        jdbcClient
                .sql(
                        """
                        UPDATE documents.chat
                        SET updated_at = NOW()
                        WHERE id = :chatId
                        """)
                .param(CHAT_ID, chatId)
                .update();
    }

    @Override
    public void delete(UUID chatId) {
        jdbcClient
                .sql("DELETE FROM documents.chat WHERE id = :chatId")
                .param(CHAT_ID, chatId)
                .update();
    }

    private Chat mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Chat(
                rs.getObject("id", UUID.class),
                rs.getLong("space_id"),
                rs.getString("owner_id"),
                rs.getString(TITLE),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant());
    }
}
