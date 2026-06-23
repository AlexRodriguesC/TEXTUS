package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.exception.DocumentDomainException;
import ch.supsi.documentapplication.model.Space;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Log4j2
public class DatabaseSpaceStorage extends DatabaseStorage implements IDatabaseSpaceStorage {

    public DatabaseSpaceStorage(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public Long create(String name, String description) {
        return jdbc.execute((ConnectionCallback<Long>) connection -> {
            try (var stmt = connection.prepareStatement("SELECT documents.create_space(?, ?)")) {

                stmt.setString(1, name);
                stmt.setString(2, description);

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
    public void update(String name, String description) {}

    @Override
    public void delete(Long id) {}
}
