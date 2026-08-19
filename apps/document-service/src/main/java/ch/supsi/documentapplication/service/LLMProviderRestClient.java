package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.exception.RemoteCallFailedException;
import ch.supsi.documentapplication.model.RawModel;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

@Log4j2
@Component
public class LLMProviderRestClient implements LLMRestClient {

    private static final String MODELS = "/models";
    public static final String INCLUDE_DEACTIVATED = "include_deactivated";
    public static final String EXCLUDE_DEPRECATED = "exclude_deprecated";

    private final RestClient providerRestClient;

    @Autowired
    public LLMProviderRestClient(RestClient providerRestClient) {
        this.providerRestClient = providerRestClient;
    }

    public record RawModelListResponse(List<RawModel> data) {}

    @Override
    public List<RawModel> getModels() {
        RawModelListResponse rawResponse = providerRestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(MODELS)
                        .queryParam(INCLUDE_DEACTIVATED, false)
                        .queryParam(EXCLUDE_DEPRECATED, true)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> handleError(response))
                .body(RawModelListResponse.class);

        if (rawResponse == null || rawResponse.data == null) {
            return Collections.emptyList();
        }
        return rawResponse.data;
    }

    private void handleError(ClientHttpResponse response) throws IOException {
        String body = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        int status = response.getStatusCode().value();
        String m = "Remote call to inference provider failed with status " + status + " and body " + body;
        log.error(m);
        throw new RemoteCallFailedException(m);
    }
}
