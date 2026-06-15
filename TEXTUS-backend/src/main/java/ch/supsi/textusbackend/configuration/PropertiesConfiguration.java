package ch.supsi.textusbackend.configuration;

import ch.supsi.textusbackend.properties.AppProperties;
import ch.supsi.textusbackend.properties.PromptProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({PromptProperties.class, AppProperties.class})
public class PropertiesConfiguration {}
