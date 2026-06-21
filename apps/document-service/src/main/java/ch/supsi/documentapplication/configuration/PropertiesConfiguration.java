package ch.supsi.documentapplication.configuration;

import ch.supsi.documentapplication.properties.AppProperties;
import ch.supsi.documentapplication.properties.PromptProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({PromptProperties.class, AppProperties.class})
public class PropertiesConfiguration {}
