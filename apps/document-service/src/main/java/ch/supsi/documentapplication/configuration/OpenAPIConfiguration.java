package ch.supsi.documentapplication.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfiguration {

    @Bean
    public OpenAPI textusOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("TEXTUS API").version("0.0.1").description("This is the API for TEXTUS"));
    }
}
