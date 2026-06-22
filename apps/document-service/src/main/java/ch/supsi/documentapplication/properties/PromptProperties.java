package ch.supsi.documentapplication.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.prompts")
@Getter
@Setter
public class PromptProperties {

    private String enrichmentSystem;
    private String enrichmentUser;

    private String mindMapSystem;
    private String mindMapUser;

}
