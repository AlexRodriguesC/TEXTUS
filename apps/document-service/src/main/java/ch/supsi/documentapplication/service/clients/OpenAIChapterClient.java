package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.model.Chapter;
import ch.supsi.documentapplication.service.async.learningpipeline.Label;
import ch.supsi.documentapplication.service.validator.Validator;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.retry.RetryTemplate;

public class OpenAIChapterClient extends Client<List<Label>, List<Chapter>> {

    private static final String LABELS = "labels";

    private final Validator<List<Chapter>, List<Label>> validator;

    public OpenAIChapterClient(
            ChatClient chatClient,
            RetryTemplate retryTemplate,
            Validator<List<Chapter>, List<Label>> chapterValidator,
            PromptTemplate promptTemplate) {
        super(chatClient, retryTemplate, promptTemplate);
        this.validator = chapterValidator;
    }

    @Override
    protected List<Chapter> call(List<Label> input) {
        Map<String, Object> params = Map.of(LABELS, input);
        var prompt = getPrompt(params);
        List<Chapter> chapters = chatClient.prompt(prompt).call().entity(new ParameterizedTypeReference<>() {});
        validator.validate(chapters, input);
        return chapters;
    }
}
