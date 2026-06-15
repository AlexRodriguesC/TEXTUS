package ch.supsi.textusbackend.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.prompts")
@Getter
@Setter
public class PromptProperties {

    private String indexExtractionSystem;
    private String indexExtractionUser;

    private String summarySystem;
    private String summaryUser;
}
