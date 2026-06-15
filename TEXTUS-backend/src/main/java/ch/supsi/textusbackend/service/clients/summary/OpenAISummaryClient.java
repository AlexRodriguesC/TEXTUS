package ch.supsi.textusbackend.service.clients.summary;

import ch.supsi.textusbackend.properties.PromptProperties;
import ch.supsi.textusbackend.service.clients.OpenAIClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class OpenAISummaryClient extends OpenAIClient implements SummaryClient {

    private final Resource promptUserResource;

    @Autowired
    public OpenAISummaryClient(
            ChatClient.Builder builder, PromptProperties promptProperties, ResourceLoader resourceLoader) {
        super(builder.defaultSystem(resourceLoader.getResource(promptProperties.getSummarySystem()))
                .build());
        this.promptUserResource = resourceLoader.getResource(promptProperties.getSummaryUser());
    }
}
