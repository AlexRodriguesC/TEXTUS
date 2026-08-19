package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.exception.AIOutputMappingFailedException;
import ch.supsi.documentapplication.model.AiContext;
import ch.supsi.documentapplication.model.ClientResult;
import ch.supsi.documentapplication.model.PageSummarization;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.util.StringUtils;

@Log4j2
public class OpenAISummarizationClient extends Client<String, PageSummarization> {

    private static final String DOCUMENT_TEXT = "document_text";

    public OpenAISummarizationClient(
            ChatClient chatClient,
            RetryTemplateWrapper retryTemplate,
            String systemPrompt,
            PromptTemplate promptTemplate,
            AiContext aiContext) {
        super(
                chatClient,
                new BeanOutputConverter<>(PageSummarization.class),
                retryTemplate,
                systemPrompt,
                promptTemplate,
                aiContext);
    }

    @Override
    protected ClientResult<PageSummarization> call(String input) {
        Map<String, Object> params = new HashMap<>();
        params.put(DOCUMENT_TEXT, input);
        ClientResult<PageSummarization> result = invoke(getPrompt(params), PageSummarization.class);
        validate(result.result());
        return result;
    }

    private static void validate(PageSummarization summarization) {
        if (!summarization.isContentPage()) {
            return;
        }
        if (!StringUtils.hasText(summarization.getTitle()) || !StringUtils.hasText(summarization.getSummary())) {
            throw AIOutputMappingFailedException.create(
                    "page summarization", "content page must have non-empty title and summary");
        }
    }
}
