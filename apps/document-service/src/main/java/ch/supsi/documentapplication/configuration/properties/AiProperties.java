package ch.supsi.documentapplication.configuration.properties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
@Getter
@Setter
public class AiProperties {

    private String provider = "llmgateway";

    private List<String> modelFallbacks = new ArrayList<>();

    private Duration longRunningRequestTimeout = Duration.ofMinutes(5);
}
