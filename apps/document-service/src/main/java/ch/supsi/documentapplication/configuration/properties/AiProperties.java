package ch.supsi.documentapplication.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
@Getter
@Setter
public class AiProperties {

    private String provider = "llmgateway";
}
