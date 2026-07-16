package ch.supsi.documentapplication.service.async.learningpipeline;

import ch.supsi.documentapplication.dataaccess.IDatabasePathStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage;
import ch.supsi.documentapplication.model.learn.Chapter;
import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.service.async.ProcessingTasklet;
import ch.supsi.documentapplication.service.clients.OpenAIChapterClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        List<Chapter> finalChapters = new ArrayList<>();

        List<Topic> remainingTopics = new ArrayList<>(topics);
        Map<Long, String> labelById = labels.stream().collect(Collectors.toMap(Label::id, Label::name));

        for (Chapter chapter : chapters) {
            List<String> chapterLabelNames =
                    chapter.getLabels().stream().map(labelById::get).toList();

            List<Topic> matchingTopics = remainingTopics.stream()
                    .filter(topic -> topic.getLabels().stream().anyMatch(chapterLabelNames::contains))
                    .toList();

            remainingTopics.removeAll(matchingTopics);

            var matchingAsLong = matchingTopics.stream().map(Topic::getId).toList();
            finalChapters.add(new Chapter(
                    chapter.getIndex(),
                    chapter.getTitle(),
                    chapter.getDescription(),
                    chapter.getLabels(),
                    matchingAsLong));
        }

        finalChapters.forEach(Chapter::buildNodes);

        for (Chapter chapter : finalChapters) {
            databaseLearningStorage.create(pathId, chapter, spaceId);
        }

        log.debug("Finished chapter generation step for space with spaceId {}", spaceId);
        return RepeatStatus.FINISHED;
    }
}
