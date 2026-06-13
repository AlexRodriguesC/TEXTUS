package ch.supsi.textusbackend.service.clients;

import ch.supsi.textusbackend.exception.PromptTemplateLoadException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

public abstract class OpenAIClient {

    protected ChatClient chatClient;

    public OpenAIClient(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Method to load a resource (in this context, a resource representing a prompt template) into a string.
     *
     * @param resource the prompt to be loaded
     * @return the prompt as string
     */
    protected String load(Resource resource) {
        try (var in = resource.getInputStream();
                var reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new PromptTemplateLoadException("Failed to load prompt template: " + resource.getDescription(), e);
        }
    }
}
