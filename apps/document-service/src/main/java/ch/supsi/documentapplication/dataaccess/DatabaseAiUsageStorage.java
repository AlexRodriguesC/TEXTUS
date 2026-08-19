package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.AiUsage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@Log4j2
public class DatabaseAiUsageStorage extends DatabaseStorage implements IDatabaseAiUsageStorage {

    private static final String ENTITY_TYPE = "entity_type";
    private static final String ENTITY_ID = "entity_id";
    private static final String OPERATION = "operation";
    private static final String TOKENS_IN = "tokens_in";
    private static final String TOKENS_OUT = "tokens_out";
    private static final String PROVIDER = "provider";
    private static final String MODEL = "model";

    @Autowired
    public DatabaseAiUsageStorage(JdbcClient jdbcClient) {
        super(jdbcClient);
    }

    @Override
    public void create(String entityType, Long entityId, String operation, AiUsage usage) {
        jdbcClient
                .sql(
                        """
                        SELECT documents.create_ai_usage(
                            :entity_type,
                            :entity_id,
                            :operation,
                            :tokens_in,
                            :tokens_out,
                            :provider,
                            :model
                        )
                        """)
                .param(ENTITY_TYPE, entityType)
                .param(ENTITY_ID, entityId)
                .param(OPERATION, operation)
                .param(TOKENS_IN, usage.tokensIn())
                .param(TOKENS_OUT, usage.tokensOut())
                .param(PROVIDER, usage.provider())
                .param(MODEL, usage.model())
                .query(Long.class)
                .single();
    }
}
