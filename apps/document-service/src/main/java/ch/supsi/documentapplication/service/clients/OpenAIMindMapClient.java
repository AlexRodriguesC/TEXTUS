package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.model.AiContext;
import ch.supsi.documentapplication.model.ClientResult;
import ch.supsi.documentapplication.model.MindMap;
import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import ch.supsi.documentapplication.service.validator.MindMapValidator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;

public class OpenAIMindMapClient extends Client<List<Topic>, MindMap> {

    private static final String TOPICS = "topics";

    private final MindMapValidator mindMapValidator;

    public OpenAIMindMapClient(
            ChatClient chatClient,
            RetryTemplateWrapper retryTemplate,
            String systemPrompt,
            PromptTemplate promptTemplate,
            MindMapValidator mindMapValidator,
            AiContext aiContext) {
        super(
                chatClient,
                new BeanOutputConverter<>(MindMap.class),
                retryTemplate,
                systemPrompt,
                promptTemplate,
                aiContext);
        this.mindMapValidator = mindMapValidator;
    }

    @Override
    protected ClientResult<MindMap> call(List<Topic> input) {
        Map<String, Object> params = new HashMap<>();
        params.put(TOPICS, input);
        var result = invoke(getPrompt(params), MindMap.class);

        var expectedChunkIds = input.stream().map(Topic::getId).toList();
        mindMapValidator.validate(result.result(), expectedChunkIds);
        return result;
    }
}
