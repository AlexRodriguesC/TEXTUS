package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.model.Page;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import tools.jackson.core.exc.StreamReadException;

@Slf4j
public class OpenAISummarizationClient extends Client<String, Page> {

    private static final String DOCUMENT_TEXT = "document_text";

    public OpenAISummarizationClient(
            ChatClient chatClient, PromptTemplate promptTemplate, FailureTracker failureTracker) {
        super(chatClient, promptTemplate, failureTracker);
    }

    @Override
    protected Optional<Page> call(String input) {
        Map<String, Object> params = Map.of(DOCUMENT_TEXT, input);
        var prompt = getPrompt(params);

        try {
            var output = chatClient.prompt(prompt).call().entity(Page.class);
            return Optional.ofNullable(output);
        } catch (StreamReadException e) {
            log.warn("Failed to parse output for summarisation prompt", e);
            return Optional.empty();
        }
    }
}
