package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.exception.DocumentDomainException;
import ch.supsi.documentapplication.model.Space;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@Log4j2
public class DatabaseSpaceStorage extends DatabaseStorage implements IDatabaseSpaceStorage {

    private final JdbcClient jdbcClient;

    public DatabaseSpaceStorage(JdbcTemplate jdbc, JdbcClient jdbcClient) {
        super(jdbc);
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Long create(String owner, String name, String description) {
        return jdbc.execute((ConnectionCallback<Long>) connection -> {
            try (var stmt = connection.prepareStatement("SELECT documents.create_space(?, ?, ?)")) {

                stmt.setString(1, owner);
                stmt.setString(2, name);
                stmt.setString(3, description);

                try (var rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                    throw new DocumentDomainException("documents.create_space returned no id");
                }
            }
        });
    }

    @Override
    public Space read(Long id) {
        return null;
    }

    @Override
    public List<Space> readAll(String owner) {
        return jdbc.execute((ConnectionCallback<List<Space>>) connection -> {
            try (var stmt = connection.prepareStatement("SELECT * FROM documents.read_spaces(?)")) {

                stmt.setString(1, owner);

                try (var rs = stmt.executeQuery()) {
                    List<Space> spaces = new ArrayList<>();

                    while (rs.next()) {
                        spaces.add(new Space(rs.getLong("id"), rs.getString("name"), rs.getString("description")));
                    }

                    return spaces;
                }
            }
        });
    }

    @Override
    public void update(Long spaceId, String name, String description) {
        jdbcClient
                .sql("SELECT documents.update_space(:spaceId, :name, :description)")
                .param("spaceId", spaceId)
                .param("name", name)
                .param("description", description)
                .query()
                .singleValue();
    }

    @Override
    public void delete(Long spaceId) {
        jdbcClient
                .sql("SELECT documents.delete_space(:spaceId)")
                .param("spaceId", spaceId)
                .query()
                .singleValue();
    }
}
