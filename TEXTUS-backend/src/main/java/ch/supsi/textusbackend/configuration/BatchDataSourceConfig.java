package ch.supsi.textusbackend.configuration;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class BatchDataSourceConfig {

    @Value("${batch.datasource.url}")
    private String url;

    @Value("${batch.datasource.username}")
    private String username;

    @Value("${batch.datasource.password}")
    private String password;

    @Value("${batch.datasource.driver-class-name}")
    private String driverClassName;

    @Bean(name = "batchDataSource")
    public DataSource batchDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }
}
