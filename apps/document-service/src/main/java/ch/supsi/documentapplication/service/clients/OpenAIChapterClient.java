package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.model.learn.Chapter;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import ch.supsi.documentapplication.service.async.learningpipeline.Label;
import ch.supsi.documentapplication.service.validator.ChapterValidator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import tools.jackson.databind.ObjectMapper;

@Slf4j
public class OpenAIChapterClient extends FeedbackLoopClient<List<Label>, List<Chapter>> {

    private static final String LABELS = "labels";
    private static final String CHAPTERS = "chapters";
    private static final String MISSING_LABEL_IDS = "missing_label_ids";

    private final ObjectMapper objectMapper;
    private final ChapterValidator validator;

    private record ChapterAssignment(int index, List<Long> labelIds) {}

    private record ChapterSkeleton(int index, String title, String description) {}

    public OpenAIChapterClient(
            ObjectMapper objectMapper,
            ChatClient chatClient,
            RetryTemplateWrapper retryTemplate,
            String systemPrompt,
            PromptTemplate promptTemplate,
            String feedbackSystemPrompt,
            PromptTemplate feedbackPromptTemplate,
            ChapterValidator validator) {
        super(
                chatClient,
                new BeanOutputConverter<>(new ParameterizedTypeReference<List<Chapter>>() {}),
                retryTemplate,
                systemPrompt,
                promptTemplate,
                feedbackSystemPrompt,
                feedbackPromptTemplate);
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @Override
    protected List<Chapter> call(List<Label> input) {
        Map<String, Object> params = new HashMap<>();
        params.put(LABELS, objectMapper.writeValueAsString(input));
        var prompt = getPrompt(params);
        List<Chapter> chapters = chatClient.prompt(prompt).call().entity(new ParameterizedTypeReference<>() {});

        var feedback = validator.extractFeedback(chapters, input);
        if (!feedback.isValid() && chapters != null) {
            List<ChapterSkeleton> skeletons = chapters.stream()
                    .map(c -> new ChapterSkeleton(c.getIndex(), c.getTitle(), c.getDescription()))
                    .toList();

            Map<String, Object> feedbackParams = Map.of(
                    CHAPTERS, objectMapper.writeValueAsString(skeletons),
                    MISSING_LABEL_IDS, objectMapper.writeValueAsString(feedback.missingLabelIds()));
            var feedbackPrompt = getFeedbackPrompt(feedbackParams);
            log.info("Feedback prompt: {}", feedbackPrompt);

            List<ChapterAssignment> assignments =
                    chatClient.prompt(feedbackPrompt).call().entity(new ParameterizedTypeReference<>() {});

            Map<Integer, List<Long>> assignmentByIndex = assignments.stream()
                    .collect(Collectors.toMap(ChapterAssignment::index, ChapterAssignment::labelIds));

            chapters = chapters.stream()
                    .map(c -> {
                        List<Long> toAdd = assignmentByIndex.getOrDefault(c.getIndex(), List.of());
                        if (toAdd.isEmpty()) return c;
                        List<Long> mergedLabelIds = new ArrayList<>(c.getLabels());
                        mergedLabelIds.addAll(toAdd);
                        return new Chapter(
                                c.getIndex(), c.getTitle(), c.getDescription(), mergedLabelIds, c.getTopics());
                    })
                    .toList();
        }

        validator.validate(chapters, input);
        return chapters;
    }
}
