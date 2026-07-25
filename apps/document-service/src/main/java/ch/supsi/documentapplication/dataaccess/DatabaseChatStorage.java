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
                .sql("SELECT documents.create_chat(:spaceId, :owner, :title)")
                .param(SPACE_ID, spaceId)
                .param(OWNER, owner)
                .param(TITLE, title)
                .query(UUID.class)
                .single();
    }

    @Override
    public List<Chat> findBySpaceAndOwner(Long spaceId, String owner) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_chats_by_space_and_owner(:spaceId, :owner)")
                .param(SPACE_ID, spaceId)
                .param(OWNER, owner)
                .query(this::mapRow)
                .list();
    }

    @Override
    public Optional<Chat> findById(UUID chatId, Long spaceId, String owner) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_chat(:chatId, :spaceId, :owner)")
                .param(CHAT_ID, chatId)
                .param(SPACE_ID, spaceId)
                .param(OWNER, owner)
                .query(this::mapRow)
                .optional();
    }

    @Override
    public void updateTitle(UUID chatId, String title) {
        jdbcClient
                .sql("SELECT documents.update_chat_title(:chatId, :title)")
                .param(CHAT_ID, chatId)
                .param(TITLE, title)
                .query()
                .singleValue();
    }

    @Override
    public void touchUpdatedAt(UUID chatId) {
        jdbcClient
                .sql("SELECT documents.touch_chat_updated_at(:chatId)")
                .param(CHAT_ID, chatId)
                .query()
                .singleValue();
    }

    @Override
    public void delete(UUID chatId) {
        jdbcClient
                .sql("SELECT documents.delete_chat(:chatId)")
                .param(CHAT_ID, chatId)
                .query()
                .singleValue();
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
