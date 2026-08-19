package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.exception.AIOutputMappingFailedException;
import ch.supsi.documentapplication.model.AiContext;
import ch.supsi.documentapplication.model.ClientResult;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import ch.supsi.documentapplication.service.async.learningpipeline.ChapterBoundary;
import ch.supsi.documentapplication.service.async.learningpipeline.ChapterGenerationRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

@Slf4j
public class OpenAIChapterClient extends Client<ChapterGenerationRequest, ChapterBoundary> {

    private static final String TOPICS = "topics";

    private final ObjectMapper objectMapper;

    public OpenAIChapterClient(
            ObjectMapper objectMapper,
            ChatClient chatClient,
            RetryTemplateWrapper retryTemplate,
            String systemPrompt,
            PromptTemplate promptTemplate,
            AiContext aiContext) {
        super(
                chatClient,
                new BeanOutputConverter<>(ChapterBoundary.class),
                retryTemplate,
                systemPrompt,
                promptTemplate,
                aiContext);
        this.objectMapper = objectMapper;
    }

    @Override
    protected ClientResult<ChapterBoundary> call(ChapterGenerationRequest input) {
        Map<String, Object> params = new HashMap<>();
        params.put(TOPICS, objectMapper.writeValueAsString(input.topics()));
        ClientResult<ChapterBoundary> result = invoke(getPrompt(params), ChapterBoundary.class);
        validate(result.result(), input.topics().size());
        return result;
    }

    private static void validate(ChapterBoundary boundary, int windowSize) {
        if (boundary == null) {
            throw AIOutputMappingFailedException.create("chapter", "boundary result is null");
        }
        if (!StringUtils.hasText(boundary.getTitle()) || !StringUtils.hasText(boundary.getDescription())) {
            throw AIOutputMappingFailedException.create("chapter", "title and description must be non-empty");
        }
        Integer cuttingPoint = boundary.getCuttingPoint();
        if (cuttingPoint == null) {
            throw AIOutputMappingFailedException.create("chapter", "cutting point is missing");
        }
        if (cuttingPoint < 1 || cuttingPoint > windowSize) {
            throw AIOutputMappingFailedException.create(
                    "chapter", "cutting point " + cuttingPoint + " is outside window of size " + windowSize);
        }
    }
}
