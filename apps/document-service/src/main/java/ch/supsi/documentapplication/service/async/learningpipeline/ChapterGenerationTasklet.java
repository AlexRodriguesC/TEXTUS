package ch.supsi.documentapplication.service.async.learningpipeline;

import ch.supsi.documentapplication.configuration.properties.AppProperties;
import ch.supsi.documentapplication.dataaccess.IDatabaseAiUsageStorage;
import ch.supsi.documentapplication.dataaccess.IDatabasePathStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage;
import ch.supsi.documentapplication.model.ClientResult;
import ch.supsi.documentapplication.model.learn.Chapter;
import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.service.async.ProcessingTasklet;
import ch.supsi.documentapplication.service.clients.OpenAIChapterClient;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@StepScope
public class ChapterGenerationTasklet extends ProcessingTasklet {

    private final IDatabaseTopicStorage databaseTopicStorage;
    private final IDatabasePathStorage databaseLearningStorage;
    private final OpenAIChapterClient openAIChapterClient;
    private final IDatabaseAiUsageStorage aiUsageStorage;
    private final AppProperties appProperties;
    private final ChapterConsolidator chapterConsolidator;

    public ChapterGenerationTasklet(
            IDatabaseTopicStorage databaseTopicStorage,
            IDatabasePathStorage databaseLearningStorage,
            OpenAIChapterClient chapterClient,
            IDatabaseAiUsageStorage aiUsageStorage,
            AppProperties appProperties,
            ChapterConsolidator chapterConsolidator) {
        this.databaseTopicStorage = databaseTopicStorage;
        this.databaseLearningStorage = databaseLearningStorage;
        this.openAIChapterClient = chapterClient;
        this.aiUsageStorage = aiUsageStorage;
        this.appProperties = appProperties;
        this.chapterConsolidator = chapterConsolidator;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext)
            throws Exception {

        log.debug("Executing chapter generation step for space with spaceId {}", spaceId);

        Long pathId =
                (Long) chunkContext.getStepContext().getJobExecutionContext().get("PATH_ID");

        List<Topic> topics = databaseTopicStorage.getBySpaceId(spaceId);
        int maxChapters = computeMaxChapters(topics.size());

        List<String> distinctLabels = topics.stream()
                .flatMap(topic -> topic.getLabels().stream())
                .distinct()
                .toList();

        List<Label> labels = IntStream.range(0, distinctLabels.size())
                .mapToObj(i -> new Label((long) i, distinctLabels.get(i)))
                .toList();

        var chapterResult = openAIChapterClient.getResult(new ChapterGenerationRequest(labels, maxChapters));
        List<Chapter> chapters = chapterResult.result();
        persistChapterUsage(pathId, chapterResult);

        Map<Long, String> labelById = labels.stream().collect(Collectors.toMap(Label::id, Label::name));

        List<Set<String>> chapterLabelNames = chapters.stream()
                .map(chapter -> chapter.getLabels().stream().map(labelById::get).collect(Collectors.toSet()))
                .toList();

        Map<Integer, List<Long>> topicsByChapterIndex = new HashMap<>();
        for (int i = 0; i < chapters.size(); i++) {
            topicsByChapterIndex.put(i, new ArrayList<>());
        }

        for (Topic topic : topics) {
            int bestChapterIndex = -1;
            int bestOverlap = 0;

            for (int i = 0; i < chapterLabelNames.size(); i++) {
                long overlap = topic.getLabels().stream()
                        .filter(chapterLabelNames.get(i)::contains)
                        .count();
                if (overlap > bestOverlap) {
                    bestOverlap = (int) overlap;
                    bestChapterIndex = i;
                }
            }

            if (bestChapterIndex == -1) {
                throw new IllegalStateException("Topic with id " + topic.getId()
                        + " matches no chapter labels; this indicates a bug upstream.");
            }

            topicsByChapterIndex.get(bestChapterIndex).add(topic.getId());
        }

        List<Chapter> assignedChapters = new ArrayList<>();
        for (int i = 0; i < chapters.size(); i++) {
            List<Long> chapterTopics = topicsByChapterIndex.get(i);
            Chapter chapter = chapters.get(i);
            assignedChapters.add(
                    new Chapter(i, chapter.getTitle(), chapter.getDescription(), chapter.getLabels(), chapterTopics));
        }

        List<Chapter> finalChapters = chapterConsolidator.consolidate(assignedChapters, maxChapters);
        finalChapters.forEach(Chapter::buildLessons);

        for (Chapter chapter : finalChapters) {
            databaseLearningStorage.create(pathId, chapter, spaceId);
        }

        log.debug("Finished chapter generation step for space with spaceId {}", spaceId);
        return RepeatStatus.FINISHED;
    }

    int computeMaxChapters(int topicCount) {
        int topicDerivedCap = Math.max(1, topicCount / 3);
        return Math.min(appProperties.getMaxChapters(), topicDerivedCap);
    }

    private void persistChapterUsage(Long pathId, ClientResult<List<Chapter>> result) {
        var operations = List.of("chapter_generate", "chapter_feedback");
        for (int i = 0; i < result.usages().size(); i++) {
            String operation = i < operations.size() ? operations.get(i) : "chapter_generate";
            aiUsageStorage.create(
                    "learning_path", pathId, operation, result.usages().get(i));
        }
    }
}
