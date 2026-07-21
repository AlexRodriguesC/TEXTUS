package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.model.AiContext;
import ch.supsi.documentapplication.model.ClientResult;
import ch.supsi.documentapplication.model.LabelMindMap;
import ch.supsi.documentapplication.model.LabelMindMapAssignment;
import ch.supsi.documentapplication.model.MindMap;
import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import ch.supsi.documentapplication.service.async.learningpipeline.Label;
import ch.supsi.documentapplication.service.mindmap.LabelMindMapTreeUtils;
import ch.supsi.documentapplication.service.mindmap.MindMapAssembler;
import ch.supsi.documentapplication.service.validator.LabelMindMapValidator;
import ch.supsi.documentapplication.service.validator.MindMapValidator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import tools.jackson.databind.ObjectMapper;

public class OpenAIMindMapClient extends Client<List<Topic>, MindMap> {

    private static final String LABELS = "labels";
    private static final String LABEL_TOPIC_GROUPS = "label_topic_groups";
    private static final String NODES = "nodes";
    private static final String MISSING_LABEL_IDS = "missing_label_ids";

    private final ObjectMapper objectMapper;
    private final MindMapAssembler mindMapAssembler;
    private final LabelMindMapValidator labelMindMapValidator;
    private final MindMapValidator mindMapValidator;
    private final BeanOutputConverter<LabelMindMap> labelMindMapConverter;
    private final String feedbackSystemPrompt;
    private final PromptTemplate feedbackPromptTemplate;

    public OpenAIMindMapClient(
            ObjectMapper objectMapper,
            ChatClient chatClient,
            RetryTemplateWrapper retryTemplate,
            String systemPrompt,
            PromptTemplate promptTemplate,
            String feedbackSystemPrompt,
            PromptTemplate feedbackPromptTemplate,
            MindMapAssembler mindMapAssembler,
            LabelMindMapValidator labelMindMapValidator,
            MindMapValidator mindMapValidator,
            AiContext aiContext) {
        super(
                chatClient,
                new BeanOutputConverter<>(MindMap.class),
                retryTemplate,
                systemPrompt,
                promptTemplate,
                aiContext);
        this.objectMapper = objectMapper;
        this.mindMapAssembler = mindMapAssembler;
        this.labelMindMapValidator = labelMindMapValidator;
        this.mindMapValidator = mindMapValidator;
        this.labelMindMapConverter = new BeanOutputConverter<>(LabelMindMap.class);
        this.feedbackSystemPrompt = feedbackSystemPrompt;
        this.feedbackPromptTemplate = feedbackPromptTemplate;
    }

    @Override
    protected ClientResult<MindMap> call(List<Topic> input) {
        List<Label> labels = MindMapAssembler.buildLabels(input);
        var labelTopicGroups = MindMapAssembler.buildLabelTopicGroups(input, labels);

        Map<String, Object> params = new HashMap<>();
        params.put(LABELS, objectMapper.writeValueAsString(labels));
        params.put(LABEL_TOPIC_GROUPS, objectMapper.writeValueAsString(labelTopicGroups));

        var primaryPrompt = getLabelMindMapPrompt(params);
        var primaryResult = invoke(primaryPrompt, LabelMindMap.class);
        List<ch.supsi.documentapplication.model.AiUsage> usages = new ArrayList<>(primaryResult.usages());
        LabelMindMap labelMindMap = primaryResult.result();

        var feedback = labelMindMapValidator.extractFeedback(labelMindMap, labels);
        if (!feedback.isValid() && labelMindMap != null && labelMindMap.getRoot() != null) {
            Map<Long, String> labelNameById = labels.stream().collect(Collectors.toMap(Label::id, Label::name));
            var skeletons = LabelMindMapTreeUtils.flattenToSkeleton(labelMindMap.getRoot(), labelNameById);

            Map<String, Object> feedbackParams = new HashMap<>();
            feedbackParams.put(NODES, objectMapper.writeValueAsString(skeletons));
            feedbackParams.put(LABELS, objectMapper.writeValueAsString(labels));
            feedbackParams.put(MISSING_LABEL_IDS, objectMapper.writeValueAsString(feedback.missingLabelIds()));

            var feedbackResult = invoke(
                    getFeedbackPrompt(feedbackParams),
                    new ParameterizedTypeReference<List<LabelMindMapAssignment>>() {});
            usages.addAll(feedbackResult.usages());

            Map<Integer, List<Long>> assignmentsByParentIndex = feedbackResult.result().stream()
                    .collect(Collectors.toMap(
                            LabelMindMapAssignment::parentNodeIndex, LabelMindMapAssignment::labelIds));
            LabelMindMapTreeUtils.mergeMissingLabels(labelMindMap, assignmentsByParentIndex);
        }

        if (labelMindMap != null) {
            var postFeedback = labelMindMapValidator.extractFeedback(labelMindMap, labels);
            if (!postFeedback.duplicateLabelIds().isEmpty()
                    || !postFeedback.extraLabelIds().isEmpty()) {
                labelMindMap = LabelMindMapTreeUtils.sanitize(labelMindMap, labels);
            }
        }

        labelMindMapValidator.validate(labelMindMap, labels);

        MindMap mindMap = mindMapAssembler.assemble(labelMindMap, input, labels);
        var expectedTopicIds = input.stream().map(Topic::getId).toList();
        mindMapValidator.validate(mindMap, expectedTopicIds);

        return withUsages(mindMap, usages);
    }

    private Prompt getLabelMindMapPrompt(Map<String, Object> params) {
        params.put(FORMAT, labelMindMapConverter.getFormat());
        var filledUserPrompt = promptTemplate.render(params);
        var systemMessage = new org.springframework.ai.chat.messages.SystemMessage(systemPrompt);
        var userMessage = UserMessage.builder().text(filledUserPrompt).build();
        return new Prompt(systemMessage, userMessage);
    }

    private Prompt getFeedbackPrompt(Map<String, Object> params) {
        params.put(FORMAT, labelMindMapConverter.getFormat());
        var filledUserPrompt = feedbackPromptTemplate.render(params);
        var systemMessage = new org.springframework.ai.chat.messages.SystemMessage(feedbackSystemPrompt);
        var userMessage = UserMessage.builder().text(filledUserPrompt).build();
        return new Prompt(systemMessage, userMessage);
    }
}
