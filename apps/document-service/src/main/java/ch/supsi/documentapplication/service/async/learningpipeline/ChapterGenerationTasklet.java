package ch.supsi.documentapplication.service.async.learningpipeline;

import ch.supsi.documentapplication.dataaccess.IDatabaseLearningStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage;
import ch.supsi.documentapplication.model.Chapter;
import ch.supsi.documentapplication.model.Topic;
import ch.supsi.documentapplication.service.ProcessingTasklet;
import ch.supsi.documentapplication.service.clients.OpenAIChapterClient;
import java.util.ArrayList;
import java.util.List;
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
    private final IDatabaseLearningStorage databaseLearningStorage;
    private final OpenAIChapterClient openAIChapterClient;

    public ChapterGenerationTasklet(
            IDatabaseTopicStorage databaseTopicStorage,
            IDatabaseLearningStorage databaseLearningStorage,
            OpenAIChapterClient chapterClient) {
        this.databaseTopicStorage = databaseTopicStorage;
        this.databaseLearningStorage = databaseLearningStorage;
        this.openAIChapterClient = chapterClient;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext)
            throws Exception {

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
        for (Chapter chapter : chapters) {
            var chapterLabels = chapter.getLabels();
            for (Label label : labels) {
                List<Topic> matchingTopics = remainingTopics.stream()
                        .filter(topic -> topic.getLabels().contains(label.name()))
                        .toList();
                remainingTopics.removeAll(matchingTopics);
                var matchingAsLong = matchingTopics.stream().map(Topic::getId).toList();
                finalChapters.add(new Chapter(
                        chapter.getIndex(),
                        chapter.getTitle(),
                        chapter.getDescription(),
                        chapterLabels,
                        matchingAsLong));
            }
        }

        for (Chapter chapter : finalChapters) {
            databaseLearningStorage.create(chapter, spaceId);
        }

        return RepeatStatus.FINISHED;
    }
}
