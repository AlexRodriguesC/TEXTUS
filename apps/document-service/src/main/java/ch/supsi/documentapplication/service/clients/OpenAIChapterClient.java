package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.model.AiContext;
import ch.supsi.documentapplication.model.AiUsage;
import ch.supsi.documentapplication.model.ClientResult;
import ch.supsi.documentapplication.model.learn.Chapter;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import ch.supsi.documentapplication.service.async.learningpipeline.ChapterGenerationRequest;
import ch.supsi.documentapplication.service.async.learningpipeline.Label;
import ch.supsi.documentapplication.service.validator.ChapterValidator;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import tools.jackson.databind.ObjectMapper;

@Slf4j
public class OpenAIChapterClient extends FeedbackLoopClient<ChapterGenerationRequest, List<Chapter>> {

    private static final String LABELS = "labels";
    private static final String MAX_CHAPTERS = "max_chapters";
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
            ChapterValidator validator,
            AiContext aiContext) {
        super(
                chatClient,
                new BeanOutputConverter<>(new ParameterizedTypeReference<>() {}),
                retryTemplate,
                systemPrompt,
                promptTemplate,
                feedbackSystemPrompt,
                feedbackPromptTemplate,
                aiContext);
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @Override
    protected ClientResult<List<Chapter>> call(ChapterGenerationRequest input) {
        List<Label> labels = input.labels();
        Map<String, Object> params = new HashMap<>();
        params.put(LABELS, objectMapper.writeValueAsString(labels));
        params.put(MAX_CHAPTERS, input.maxChapters());
        var prompt = getPrompt(params);
        var primaryResult = invoke(prompt, new ParameterizedTypeReference<List<Chapter>>() {});

        List<AiUsage> usages = new ArrayList<>(primaryResult.usages());
        List<Chapter> chapters = primaryResult.result();

        var feedback = validator.extractFeedback(chapters, labels);
        if (!feedback.isValid() && chapters != null) {
            List<ChapterSkeleton> skeletons = chapters.stream()
                    .map(c -> new ChapterSkeleton(c.getIndex(), c.getTitle(), c.getDescription()))
                    .toList();

            Map<String, Object> feedbackParams = new HashMap<>();
            feedbackParams.put(CHAPTERS, objectMapper.writeValueAsString(skeletons));
            feedbackParams.put(MISSING_LABEL_IDS, objectMapper.writeValueAsString(feedback.missingLabelIds()));
            var feedbackPrompt = getFeedbackPrompt(feedbackParams);

            var feedbackResult = invoke(feedbackPrompt, new ParameterizedTypeReference<List<ChapterAssignment>>() {});
            usages.addAll(feedbackResult.usages());

            List<ChapterAssignment> assignments = feedbackResult.result();
            Map<Integer, List<Long>> assignmentByIndex = assignments.stream()
                    .collect(Collectors.toMap(ChapterAssignment::index, ChapterAssignment::labelIds));

            chapters = chapters.stream()
                    .map(c -> {
                        List<Long> toAdd = assignmentByIndex.getOrDefault(c.getIndex(), new ArrayList<>());
                        if (toAdd.isEmpty()) return c;
                        List<Long> mergedLabelIds = new ArrayList<>(
                                Optional.ofNullable(c.getLabels()).orElse(new ArrayList<>()));
                        mergedLabelIds.addAll(toAdd);
                        return new Chapter(
                                c.getIndex(), c.getTitle(), c.getDescription(), mergedLabelIds, c.getTopics());
                    })
                    .toList();
        }

        if (chapters != null) {
            var postFeedback = validator.extractFeedback(chapters, labels);
            if (!postFeedback.duplicateLabelIds().isEmpty()
                    || !postFeedback.extraLabelIds().isEmpty()) {
                chapters = sanitizeChapters(chapters, labels);
            }
        }

        validator.validate(chapters, labels);
        return withUsages(chapters, usages);
    }

    private static List<Chapter> sanitizeChapters(List<Chapter> chapters, List<Label> expectedLabels) {
        Set<Long> expectedIds = expectedLabels.stream().map(Label::id).collect(Collectors.toSet());
        Set<Long> seen = new HashSet<>();
        List<Chapter> result = new ArrayList<>();
        int index = 0;
        for (Chapter chapter : chapters) {
            List<Long> sanitized = new ArrayList<>();
            for (Long labelId : chapter.getLabels()) {
                if (!expectedIds.contains(labelId)) {
                    continue;
                }
                if (!seen.add(labelId)) {
                    continue;
                }
                sanitized.add(labelId);
            }
            if (sanitized.isEmpty()) {
                continue;
            }
            result.add(
                    new Chapter(index, chapter.getTitle(), chapter.getDescription(), sanitized, chapter.getTopics()));
            index++;
        }
        return result;
    }
}
