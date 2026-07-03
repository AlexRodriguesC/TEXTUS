package ch.supsi.documentapplication.configuration;

import ch.supsi.documentapplication.configuration.properties.AppProperties;
import ch.supsi.documentapplication.controller.DTO.mapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientsConfiguration {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public RestClient providerRestClient(AppProperties appProperties) {
        return RestClient.builder()
                .baseUrl(appProperties.getProviderModelsUrl())
                .build();
    }
}
