package ch.supsi.textusbackend.configuration;

import javax.sql.DataSource;
import org.springframework.boot.batch.jdbc.autoconfigure.BatchDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfiguration {

    @Bean(defaultCandidate = false)
    @BatchDataSource
    @ConfigurationProperties("app.batch-datasource")
    public DataSource batchDataSource() {
        return DataSourceBuilder.create().build();
    }
}
