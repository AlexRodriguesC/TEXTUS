package ch.supsi.textusbackend.service.clients.index;

import ch.supsi.textusbackend.model.BookIndex;
import ch.supsi.textusbackend.service.clients.OpenAIClient;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class OpenAIIndexExtractionClient extends OpenAIClient implements IndexExtractionClient {

    private final Resource promptUserResource;

    @Autowired
    public OpenAIIndexExtractionClient(
            ChatClient.Builder builder,
            @Value("classpath:/prompts/index-extraction-system-v1.st") Resource promptSystemResource,
            @Value("classpath:/prompts/index-extraction-user-v1.st") Resource promptUserResource) {
        super(builder.defaultSystem(promptSystemResource).build());
        this.promptUserResource = promptUserResource;
    }

    @Override
    public BookIndex extractBookIndex(List<Media> pages) {
        var userPromptText = load(promptUserResource);
        var userMessage =
                UserMessage.builder().text(userPromptText).media(pages).build();
        var prompt = new Prompt(userMessage);

        return chatClient.prompt(prompt).call().entity(BookIndex.class);
    }
}
