package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.model.Page;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;

@Log4j2
public class OpenAISummarizationClient extends Client<String, Page> {

    private static final String DOCUMENT_TEXT = "document_text";

    public OpenAISummarizationClient(
            ChatClient chatClient, RetryTemplateWrapper retryTemplate, PromptTemplate promptTemplate) {
        super(chatClient, retryTemplate, promptTemplate);
    }

    @Override
    protected Page call(String input) {
        Map<String, Object> params = Map.of(DOCUMENT_TEXT, input);
        var prompt = getPrompt(params);

        return chatClient.prompt(prompt).call().entity(Page.class);
    }
}
