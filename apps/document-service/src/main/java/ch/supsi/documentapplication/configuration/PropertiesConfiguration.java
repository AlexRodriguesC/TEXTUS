package ch.supsi.documentapplication.configuration;

import ch.supsi.documentapplication.configuration.properties.AiProperties;
import ch.supsi.documentapplication.configuration.properties.AppProperties;
import ch.supsi.documentapplication.configuration.properties.ChatRagProperties;
import ch.supsi.documentapplication.configuration.properties.EvalServiceProperties;
import ch.supsi.documentapplication.configuration.properties.PromptProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    PromptProperties.class,
    AppProperties.class,
    AiProperties.class,
    EvalServiceProperties.class,
    ChatRagProperties.class
})
public class PropertiesConfiguration {}
