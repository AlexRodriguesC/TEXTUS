package ch.supsi.documentapplication.service.async.learningpipeline;

import ch.supsi.documentapplication.dataaccess.IDatabasePathStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage;
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

    public ChapterGenerationTasklet(
            IDatabaseTopicStorage databaseTopicStorage,
            IDatabasePathStorage databaseLearningStorage,
            OpenAIChapterClient chapterClient) {
        this.databaseTopicStorage = databaseTopicStorage;
        this.databaseLearningStorage = databaseLearningStorage;
        this.openAIChapterClient = chapterClient;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext)
            throws Exception {

        log.debug("Executing chapter generation step for space with spaceId {}", spaceId);

        Long pathId =
                (Long) chunkContext.getStepContext().getJobExecutionContext().get("PATH_ID");

        List<Topic> topics = databaseTopicStorage.getBySpaceId(spaceId);
        List<String> distinctLabels = topics.stream()
                .flatMap(topic -> topic.getLabels().stream())
                .distinct()
                .toList();

        List<Label> labels = IntStream.range(0, distinctLabels.size())
                .mapToObj(i -> new Label((long) i, distinctLabels.get(i)))
                .toList();

        List<Chapter> chapters = openAIChapterClient.getResult(labels);
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

        List<Chapter> finalChapters = new ArrayList<>();
        int nextIndex = 0;
        for (int i = 0; i < chapters.size(); i++) {
            List<Long> chapterTopics = topicsByChapterIndex.get(i);
            if (chapterTopics.isEmpty()) {
                log.debug(
                        "Dropping chapter '{}' for space {} because it has no matching topics",
                        chapters.get(i).getTitle(),
                        spaceId);
                continue;
            }

            Chapter chapter = chapters.get(i);
            finalChapters.add(new Chapter(
                    nextIndex++, chapter.getTitle(), chapter.getDescription(), chapter.getLabels(), chapterTopics));
        }

        finalChapters.forEach(Chapter::buildLessons);

        for (Chapter chapter : finalChapters) {
            databaseLearningStorage.create(pathId, chapter, spaceId);
        }

        log.debug("Finished chapter generation step for space with spaceId {}", spaceId);
        return RepeatStatus.FINISHED;
    }
}
