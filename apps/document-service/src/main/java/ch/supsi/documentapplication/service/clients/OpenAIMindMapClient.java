package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.model.AiContext;
import ch.supsi.documentapplication.model.AiUsage;
import ch.supsi.documentapplication.model.ClientResult;
import ch.supsi.documentapplication.model.MindMap;
import ch.supsi.documentapplication.model.MindMapGenerationInput;
import ch.supsi.documentapplication.model.Page;
import ch.supsi.documentapplication.model.PageMindMap;
import ch.supsi.documentapplication.model.PageMindMapAssignment;
import ch.supsi.documentapplication.model.PageMindMapChildSelection;
import ch.supsi.documentapplication.model.PageMindMapNode;
import ch.supsi.documentapplication.model.PageMindMapRootSelection;
import ch.supsi.documentapplication.model.PageTopicGroup;
import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import ch.supsi.documentapplication.service.mindmap.IncrementalPageMindMapBuilder;
import ch.supsi.documentapplication.service.mindmap.MindMapAssembler;
import ch.supsi.documentapplication.service.mindmap.MindMapExtractionSteps;
import ch.supsi.documentapplication.service.mindmap.PageMindMapTreeUtils;
import ch.supsi.documentapplication.service.validator.MindMapValidator;
import ch.supsi.documentapplication.service.validator.PageMindMapValidator;
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

public class OpenAIMindMapClient extends Client<MindMapGenerationInput, MindMap> {

    private static final String PAGES = "pages";
    private static final String PAGE_TOPIC_GROUPS = "page_topic_groups";
    private static final String PARENT_PAGE = "parent_page";
    private static final String REMAINING_PAGES = "remaining_pages";
    private static final String NODES = "nodes";
    private static final String MISSING_PAGE_IDS = "missing_page_ids";
    private static final String OPERATION_GENERATE = "mind_map_generate";
    private static final String OPERATION_FEEDBACK = "mind_map_feedback";

    private final ObjectMapper objectMapper;
    private final MindMapAssembler mindMapAssembler;
    private final PageMindMapValidator pageMindMapValidator;
    private final MindMapValidator mindMapValidator;
    private final IncrementalPageMindMapBuilder incrementalBuilder;
    private final BeanOutputConverter<PageMindMapRootSelection> rootConverter;
    private final BeanOutputConverter<PageMindMapChildSelection> childConverter;
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
            PageMindMapValidator pageMindMapValidator,
            MindMapValidator mindMapValidator,
            IncrementalPageMindMapBuilder incrementalBuilder,
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
        this.pageMindMapValidator = pageMindMapValidator;
        this.mindMapValidator = mindMapValidator;
        this.incrementalBuilder = incrementalBuilder;
        this.rootConverter = new BeanOutputConverter<>(PageMindMapRootSelection.class);
        this.childConverter = new BeanOutputConverter<>(PageMindMapChildSelection.class);
        this.rootSystemPrompt = rootSystemPrompt;
        this.rootPromptTemplate = rootPromptTemplate;
        this.childrenSystemPrompt = childrenSystemPrompt;
        this.childrenPromptTemplate = childrenPromptTemplate;
        this.feedbackSystemPrompt = feedbackSystemPrompt;
        this.feedbackPromptTemplate = feedbackPromptTemplate;
    }

    @Override
    protected ClientResult<MindMap> call(MindMapGenerationInput input) {
        List<Page> pages = input.pages();
        var topics = input.topics();
        var pageTopicGroups = MindMapAssembler.buildPageTopicGroups(pages, topics);

        List<AiUsage> usages = new ArrayList<>();
        List<String> usageOperations = new ArrayList<>();

        MindMapExtractionSteps steps = new MindMapExtractionSteps() {
            @Override
            public Long selectRoot(List<Page> allPages, List<PageTopicGroup> groups) {
                Map<String, Object> params = new HashMap<>();
                params.put(PAGES, objectMapper.writeValueAsString(allPages));
                params.put(PAGE_TOPIC_GROUPS, objectMapper.writeValueAsString(groups));

                var result = invoke(getRootPrompt(params), PageMindMapRootSelection.class);
                recordUsage(result);
                return result.result() != null ? result.result().getPageId() : null;
            }

            @Override
            public List<Long> selectChildren(
                    PageMindMapNode parent, List<Page> remaining, List<PageTopicGroup> groups) {
                Map<String, Object> params = new HashMap<>();
                params.put(PARENT_PAGE, objectMapper.writeValueAsString(findPage(parent, pages)));
                params.put(REMAINING_PAGES, objectMapper.writeValueAsString(remaining));
                params.put(PAGE_TOPIC_GROUPS, objectMapper.writeValueAsString(groups));

                var result = invoke(getChildrenPrompt(params), PageMindMapChildSelection.class);
                recordUsage(result);
                return result.result() != null ? result.result().getPageIds() : List.of();
            }

            private void recordUsage(ClientResult<?> result) {
                usages.addAll(result.usages());
                for (int i = 0; i < result.usages().size(); i++) {
                    usageOperations.add(OPERATION_GENERATE);
                }
            }
        };

        PageMindMap pageMindMap = incrementalBuilder.build(pages, pageTopicGroups, steps);

        var feedback = pageMindMapValidator.extractFeedback(pageMindMap, pages);
        if (!feedback.isValid() && pageMindMap != null && pageMindMap.getRoot() != null) {
            Map<Long, String> pageTitleById = pages.stream().collect(Collectors.toMap(Page::getId, Page::getTitle));
            var skeletons = PageMindMapTreeUtils.flattenToSkeleton(pageMindMap.getRoot(), pageTitleById);

            Map<String, Object> feedbackParams = new HashMap<>();
            feedbackParams.put(NODES, objectMapper.writeValueAsString(skeletons));
            feedbackParams.put(PAGES, objectMapper.writeValueAsString(pages));
            feedbackParams.put(MISSING_PAGE_IDS, objectMapper.writeValueAsString(feedback.missingPageIds()));

            var feedbackResult = invoke(
                    getFeedbackPrompt(feedbackParams),
                    new ParameterizedTypeReference<List<PageMindMapAssignment>>() {});
            usages.addAll(feedbackResult.usages());
            for (int i = 0; i < feedbackResult.usages().size(); i++) {
                usageOperations.add(OPERATION_FEEDBACK);
            }

            Map<Integer, List<Long>> assignmentsByParentIndex = feedbackResult.result().stream()
                    .collect(Collectors.toMap(
                            PageMindMapAssignment::parentNodeIndex,
                            PageMindMapAssignment::pageIds,
                            (existing, incoming) -> {
                                List<Long> merged = new ArrayList<>(existing);
                                merged.addAll(incoming);
                                return merged;
                            }));
            PageMindMapTreeUtils.mergeMissingPages(pageMindMap, assignmentsByParentIndex);
        }

        if (pageMindMap != null) {
            var postFeedback = pageMindMapValidator.extractFeedback(pageMindMap, pages);
            if (!postFeedback.duplicatePageIds().isEmpty()
                    || !postFeedback.extraPageIds().isEmpty()) {
                pageMindMap = PageMindMapTreeUtils.sanitize(pageMindMap, pages);
            }
        }

        pageMindMapValidator.validate(pageMindMap, pages);

        MindMap mindMap = mindMapAssembler.assemble(pageMindMap, pages, topics);
        var expectedTopicIds = topics.stream().map(Topic::getId).toList();
        mindMapValidator.validate(mindMap, expectedTopicIds);

        return new ClientResult<>(mindMap, usages, usageOperations);
    }

    private Page findPage(PageMindMapNode parent, List<Page> pages) {
        return pages.stream()
                .filter(page -> page.getId().equals(parent.getPageId()))
                .findFirst()
                .orElseThrow();
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
