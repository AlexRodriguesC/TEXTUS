package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.model.AiContext;
import ch.supsi.documentapplication.model.AiUsage;
import ch.supsi.documentapplication.model.ClientResult;
import ch.supsi.documentapplication.model.LabelMindMap;
import ch.supsi.documentapplication.model.LabelMindMapAssignment;
import ch.supsi.documentapplication.model.LabelMindMapChildSelection;
import ch.supsi.documentapplication.model.LabelMindMapNode;
import ch.supsi.documentapplication.model.LabelMindMapRootSelection;
import ch.supsi.documentapplication.model.LabelTopicGroup;
import ch.supsi.documentapplication.model.MindMap;
import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import ch.supsi.documentapplication.service.async.learningpipeline.Label;
import ch.supsi.documentapplication.service.mindmap.IncrementalLabelMindMapBuilder;
import ch.supsi.documentapplication.service.mindmap.LabelMindMapTreeUtils;
import ch.supsi.documentapplication.service.mindmap.MindMapAssembler;
import ch.supsi.documentapplication.service.mindmap.MindMapExtractionSteps;
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
    private static final String PARENT_LABEL = "parent_label";
    private static final String REMAINING_LABELS = "remaining_labels";
    private static final String NODES = "nodes";
    private static final String MISSING_LABEL_IDS = "missing_label_ids";
    private static final String OPERATION_GENERATE = "mind_map_generate";
    private static final String OPERATION_FEEDBACK = "mind_map_feedback";

    private final ObjectMapper objectMapper;
    private final MindMapAssembler mindMapAssembler;
    private final LabelMindMapValidator labelMindMapValidator;
    private final MindMapValidator mindMapValidator;
    private final IncrementalLabelMindMapBuilder incrementalBuilder;
    private final BeanOutputConverter<LabelMindMapRootSelection> rootConverter;
    private final BeanOutputConverter<LabelMindMapChildSelection> childConverter;
    private final String rootSystemPrompt;
    private final PromptTemplate rootPromptTemplate;
    private final String childrenSystemPrompt;
    private final PromptTemplate childrenPromptTemplate;
    private final String feedbackSystemPrompt;
    private final PromptTemplate feedbackPromptTemplate;

    public OpenAIMindMapClient(
            ObjectMapper objectMapper,
            ChatClient chatClient,
            RetryTemplateWrapper retryTemplate,
            String rootSystemPrompt,
            PromptTemplate rootPromptTemplate,
            String childrenSystemPrompt,
            PromptTemplate childrenPromptTemplate,
            String feedbackSystemPrompt,
            PromptTemplate feedbackPromptTemplate,
            MindMapAssembler mindMapAssembler,
            LabelMindMapValidator labelMindMapValidator,
            MindMapValidator mindMapValidator,
            IncrementalLabelMindMapBuilder incrementalBuilder,
            AiContext aiContext) {
        super(
                chatClient,
                new BeanOutputConverter<>(MindMap.class),
                retryTemplate,
                "",
                PromptTemplate.builder().template("unused").build(),
                aiContext);
        this.objectMapper = objectMapper;
        this.mindMapAssembler = mindMapAssembler;
        this.labelMindMapValidator = labelMindMapValidator;
        this.mindMapValidator = mindMapValidator;
        this.incrementalBuilder = incrementalBuilder;
        this.rootConverter = new BeanOutputConverter<>(LabelMindMapRootSelection.class);
        this.childConverter = new BeanOutputConverter<>(LabelMindMapChildSelection.class);
        this.rootSystemPrompt = rootSystemPrompt;
        this.rootPromptTemplate = rootPromptTemplate;
        this.childrenSystemPrompt = childrenSystemPrompt;
        this.childrenPromptTemplate = childrenPromptTemplate;
        this.feedbackSystemPrompt = feedbackSystemPrompt;
        this.feedbackPromptTemplate = feedbackPromptTemplate;
    }

    @Override
    protected ClientResult<MindMap> call(List<Topic> input) {
        List<Label> labels = MindMapAssembler.buildLabels(input);
        var labelTopicGroups = MindMapAssembler.buildLabelTopicGroups(input, labels);

        List<AiUsage> usages = new ArrayList<>();
        List<String> usageOperations = new ArrayList<>();

        MindMapExtractionSteps steps = new MindMapExtractionSteps() {
            @Override
            public Long selectRoot(List<Label> allLabels, List<LabelTopicGroup> groups) {
                Map<String, Object> params = new HashMap<>();
                params.put(LABELS, objectMapper.writeValueAsString(allLabels));
                params.put(LABEL_TOPIC_GROUPS, objectMapper.writeValueAsString(groups));

                var result = invoke(getRootPrompt(params), LabelMindMapRootSelection.class);
                recordUsage(result);
                return result.result() != null ? result.result().getLabelId() : null;
            }

            @Override
            public List<Long> selectChildren(
                    LabelMindMapNode parent, List<Label> remaining, List<LabelTopicGroup> groups) {
                Map<String, Object> params = new HashMap<>();
                params.put(
                        PARENT_LABEL,
                        objectMapper.writeValueAsString(new Label(parent.getLabelId(), findLabelName(parent, labels))));
                params.put(REMAINING_LABELS, objectMapper.writeValueAsString(remaining));
                params.put(LABEL_TOPIC_GROUPS, objectMapper.writeValueAsString(groups));

                var result = invoke(getChildrenPrompt(params), LabelMindMapChildSelection.class);
                recordUsage(result);
                return result.result() != null ? result.result().getLabelIds() : List.of();
            }

            private void recordUsage(ClientResult<?> result) {
                usages.addAll(result.usages());
                for (int i = 0; i < result.usages().size(); i++) {
                    usageOperations.add(OPERATION_GENERATE);
                }
            }
        };

        LabelMindMap labelMindMap = incrementalBuilder.build(labels, labelTopicGroups, steps);

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
            for (int i = 0; i < feedbackResult.usages().size(); i++) {
                usageOperations.add(OPERATION_FEEDBACK);
            }

            Map<Integer, List<Long>> assignmentsByParentIndex = feedbackResult.result().stream()
                    .collect(Collectors.toMap(
                            LabelMindMapAssignment::parentNodeIndex,
                            LabelMindMapAssignment::labelIds,
                            (existing, incoming) -> {
                                List<Long> merged = new ArrayList<>(existing);
                                merged.addAll(incoming);
                                return merged;
                            }));
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

        return new ClientResult<>(mindMap, usages, usageOperations);
    }

    private String findLabelName(LabelMindMapNode parent, List<Label> labels) {
        return labels.stream()
                .filter(label -> label.id().equals(parent.getLabelId()))
                .map(Label::name)
                .findFirst()
                .orElse("");
    }

    private Prompt getRootPrompt(Map<String, Object> params) {
        params.put(FORMAT, rootConverter.getFormat());
        var filledUserPrompt = rootPromptTemplate.render(params);
        var systemMessage = new org.springframework.ai.chat.messages.SystemMessage(rootSystemPrompt);
        var userMessage = UserMessage.builder().text(filledUserPrompt).build();
        return new Prompt(systemMessage, userMessage);
    }

    private Prompt getChildrenPrompt(Map<String, Object> params) {
        params.put(FORMAT, childConverter.getFormat());
        var filledUserPrompt = childrenPromptTemplate.render(params);
        var systemMessage = new org.springframework.ai.chat.messages.SystemMessage(childrenSystemPrompt);
        var userMessage = UserMessage.builder().text(filledUserPrompt).build();
        return new Prompt(systemMessage, userMessage);
    }

    private Prompt getFeedbackPrompt(Map<String, Object> params) {
        var filledUserPrompt = feedbackPromptTemplate.render(params);
        var systemMessage = new org.springframework.ai.chat.messages.SystemMessage(feedbackSystemPrompt);
        var userMessage = UserMessage.builder().text(filledUserPrompt).build();
        return new Prompt(systemMessage, userMessage);
    }
}
