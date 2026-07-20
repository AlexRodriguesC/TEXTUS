package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.model.learn.Chapter;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import ch.supsi.documentapplication.service.async.learningpipeline.Label;
import ch.supsi.documentapplication.service.validator.ChapterValidator;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.ParameterizedTypeReference;
import tools.jackson.databind.ObjectMapper;

@Slf4j
public class OpenAIChapterClient extends FeedbackLoopClient<List<Label>, List<Chapter>> {

    private static final String LABELS = "labels";
    private static final String PREVIOUS_OUTPUT = "previous_output";
    private static final String MISSING_LABEL_IDS = "missing_label_ids";

    private final ObjectMapper objectMapper;
    private final ChapterValidator validator;

    public OpenAIChapterClient(
            ObjectMapper objectMapper,
            ChatClient chatClient,
            RetryTemplateWrapper retryTemplate,
            String systemPrompt,
            PromptTemplate promptTemplate,
            String feedbackSystemPrompt,
            PromptTemplate feedbackPromptTemplate,
            ChapterValidator validator) {
        super(chatClient, retryTemplate, systemPrompt, promptTemplate, feedbackSystemPrompt, feedbackPromptTemplate);
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @Override
    protected List<Chapter> call(List<Label> input) {
        Map<String, Object> params = Map.of(LABELS, input);
        var prompt = getPrompt(params);
        List<Chapter> chapters = chatClient.prompt(prompt).call().entity(new ParameterizedTypeReference<>() {});

        var feedback = validator.extractFeedback(chapters, input);
        if (!feedback.isValid() && chapters != null) {
            Map<String, Object> feedbackParams = Map.of(
                    PREVIOUS_OUTPUT, objectMapper.writeValueAsString(chapters),
                    MISSING_LABEL_IDS, objectMapper.writeValueAsString(feedback.missingLabelIds()));
            var feedbackPrompt = getFeedbackPrompt(feedbackParams);
            log.info("Feedback prompt: {}", feedbackPrompt);
            chapters = chatClient.prompt(feedbackPrompt).call().entity(new ParameterizedTypeReference<>() {});
        }

        validator.validate(chapters, input);
        return chapters;
    }
}
