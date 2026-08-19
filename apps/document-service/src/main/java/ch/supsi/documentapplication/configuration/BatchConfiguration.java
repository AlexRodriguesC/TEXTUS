package ch.supsi.documentapplication.configuration;

import javax.sql.DataSource;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.configuration.support.JdbcDefaultBatchConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.batch.autoconfigure.BatchTaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfiguration extends JdbcDefaultBatchConfiguration {

    private final DataSource batchDataSource;
    private final TaskExecutor batchTaskExecutor;

    @Autowired
    public BatchConfiguration(DataSource batchDataSource, @BatchTaskExecutor TaskExecutor batchTaskExecutor) {
        this.batchDataSource = batchDataSource;
        this.batchTaskExecutor = batchTaskExecutor;
    }

    @Override
    protected @NotNull TaskExecutor getTaskExecutor() {
        return batchTaskExecutor;
    }

    @Override
    protected @NotNull DataSource getDataSource() {
        return batchDataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new JdbcTransactionManager(batchDataSource);
    }
}
