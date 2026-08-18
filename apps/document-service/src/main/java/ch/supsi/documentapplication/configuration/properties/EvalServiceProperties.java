package ch.supsi.documentapplication.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.eval-service")
@Getter
@Setter
public class EvalServiceProperties {

    private boolean enabled = true;

    private String baseUrl = "http://localhost:8000";

    private String evaluationsEndpoint = "/api/v1/evaluations";
}
