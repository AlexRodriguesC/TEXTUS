package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.model.AiContext;
import ch.supsi.documentapplication.model.ClientResult;
import ch.supsi.documentapplication.model.Page;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;

@Log4j2
public class OpenAISummarizationClient extends Client<String, Page> {

    private static final String DOCUMENT_TEXT = "document_text";

    public OpenAISummarizationClient(
            ChatClient chatClient,
            RetryTemplateWrapper retryTemplate,
            String systemPrompt,
            PromptTemplate promptTemplate,
            AiContext aiContext) {
        super(
                chatClient,
                new BeanOutputConverter<>(Page.class),
                retryTemplate,
                systemPrompt,
                promptTemplate,
                aiContext);
    }

    @Override
    protected ClientResult<Page> call(String input) {
        Map<String, Object> params = new HashMap<>();
        params.put(DOCUMENT_TEXT, input);
        return invoke(getPrompt(params), Page.class);
    }
}
