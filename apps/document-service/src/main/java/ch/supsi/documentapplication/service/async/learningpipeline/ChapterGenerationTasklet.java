package ch.supsi.documentapplication.service.async.learningpipeline;

import ch.supsi.documentapplication.dataaccess.IDatabaseAiUsageStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage;
import ch.supsi.documentapplication.dataaccess.IDatabasePathStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage;
import ch.supsi.documentapplication.model.ClientResult;
import ch.supsi.documentapplication.model.Document;
import ch.supsi.documentapplication.model.learn.Chapter;
import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.service.async.ProcessingTasklet;
import ch.supsi.documentapplication.service.clients.OpenAIChapterClient;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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

    static final int WINDOW_SIZE = 100;

    private final IDatabaseDocumentStorage databaseDocumentStorage;
    private final IDatabaseTopicStorage databaseTopicStorage;
    private final IDatabasePathStorage databaseLearningStorage;
    private final OpenAIChapterClient openAIChapterClient;
    private final IDatabaseAiUsageStorage aiUsageStorage;

    public ChapterGenerationTasklet(
            IDatabaseDocumentStorage databaseDocumentStorage,
            IDatabaseTopicStorage databaseTopicStorage,
            IDatabasePathStorage databaseLearningStorage,
            OpenAIChapterClient chapterClient,
            IDatabaseAiUsageStorage aiUsageStorage) {
        this.databaseDocumentStorage = databaseDocumentStorage;
        this.databaseTopicStorage = databaseTopicStorage;
        this.databaseLearningStorage = databaseLearningStorage;
        this.openAIChapterClient = chapterClient;
        this.aiUsageStorage = aiUsageStorage;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext)
            throws Exception {

        log.debug("Executing chapter generation step for space with spaceId {}", spaceId);

        Long pathId =
                (Long) chunkContext.getStepContext().getJobExecutionContext().get("PATH_ID");

        int globalChapterIndex = 0;
        for (Document document : databaseDocumentStorage.getDocuments(spaceId)) {
            List<Topic> topics = databaseTopicStorage.getByDocumentId(document.getId());
            if (topics.isEmpty()) {
                throw new IllegalStateException(
                        "Document with id " + document.getId() + " has no topics; chapter generation cannot continue.");
            }

            List<Topic> remaining = new ArrayList<>(orderTopicsByPage(topics));
            while (!remaining.isEmpty()) {
                int windowSize = Math.min(WINDOW_SIZE, remaining.size());
                List<Topic> window = remaining.subList(0, windowSize);
                List<ChapterTopic> chapterTopics = toChapterTopics(window);

                ClientResult<ChapterBoundary> boundaryResult =
                        openAIChapterClient.getResult(new ChapterGenerationRequest(chapterTopics));
                persistChapterUsage(pathId, boundaryResult);

                ChapterBoundary boundary = boundaryResult.result();
                int cuttingPoint = boundary.getCuttingPoint();
                List<Long> chapterTopicIds = window.subList(0, cuttingPoint).stream()
                        .sorted(Comparator.comparingInt(ChapterGenerationTasklet::pageNumberOf))
                        .map(Topic::getId)
                        .toList();

                Chapter persistedChapter = new Chapter(
                        globalChapterIndex++,
                        boundary.getTitle(),
                        boundary.getDescription(),
                        chapterTopicIds,
                        document.getId());
                persistedChapter.buildLessons();
                databaseLearningStorage.create(pathId, persistedChapter, spaceId);

                remaining.subList(0, cuttingPoint).clear();
            }
        }

        log.debug("Finished chapter generation step for space with spaceId {}", spaceId);
        return RepeatStatus.FINISHED;
    }

    private static List<Topic> orderTopicsByPage(List<Topic> topics) {
        return topics.stream()
                .sorted(Comparator.comparingInt(ChapterGenerationTasklet::pageNumberOf)
                        .thenComparingLong(Topic::getId))
                .toList();
    }

    private static List<ChapterTopic> toChapterTopics(List<Topic> window) {
        return IntStream.range(0, window.size())
                .mapToObj(i -> new ChapterTopic(i + 1, joinLabels(window.get(i))))
                .toList();
    }

    private static String joinLabels(Topic topic) {
        List<String> labels = Optional.ofNullable(topic.getLabels()).orElse(List.of());
        return String.join(" ", labels);
    }

    private static int pageNumberOf(Topic topic) {
        return Optional.ofNullable(topic.getPageNumber())
                .orElseThrow(() ->
                        new IllegalStateException("Topic with id " + topic.getId() + " is missing a page number."));
    }

    private void persistChapterUsage(Long pathId, ClientResult<ChapterBoundary> result) {
        for (var usage : result.usages()) {
            aiUsageStorage.create("learning_path", pathId, "chapter_generate", usage);
        }
    }
}
