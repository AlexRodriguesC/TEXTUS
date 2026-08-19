package ch.supsi.documentapplication.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.chat.rag")
@Getter
@Setter
public class ChatRagProperties {

    private double similarityThreshold = 0.5;
    private int topK = 5;
    private int fallbackTopicLimit = 3;
}
