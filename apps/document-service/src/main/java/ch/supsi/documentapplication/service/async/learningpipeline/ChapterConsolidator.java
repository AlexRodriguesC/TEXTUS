package ch.supsi.documentapplication.service.async.learningpipeline;

import ch.supsi.documentapplication.model.learn.Chapter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ChapterConsolidator {

    private static final int MIN_TOPICS_PER_CHAPTER = 3;

    public List<Chapter> consolidate(List<Chapter> chapters, int maxChapters) {
        Objects.requireNonNull(chapters, "chapters cannot be null");
        if (maxChapters < 1) {
            throw new IllegalArgumentException("maxChapters must be at least 1");
        }

        List<Chapter> working = dropEmpty(chapters);
        if (working.isEmpty()) {
            return List.of();
        }

        int totalTopics = working.stream().mapToInt(c -> topicCount(c)).sum();
        int effectiveMinTopics = totalTopics < MIN_TOPICS_PER_CHAPTER ? 1 : MIN_TOPICS_PER_CHAPTER;
        int effectiveMaxChapters = Math.min(maxChapters, Math.max(1, totalTopics / effectiveMinTopics));

        while (needsMerge(working, effectiveMinTopics, effectiveMaxChapters)) {
            int sourceIndex = selectSourceIndex(working, effectiveMinTopics, effectiveMaxChapters);
            int targetIndex = selectTargetIndex(working, sourceIndex);
            working = mergeAt(working, sourceIndex, targetIndex);
        }

        return reindex(working);
    }

    private static boolean needsMerge(List<Chapter> chapters, int minTopics, int maxChapters) {
        if (chapters.size() <= 1) {
            return false;
        }
        if (chapters.size() > maxChapters) {
            return true;
        }
        return chapters.stream().anyMatch(c -> topicCount(c) < minTopics);
    }

    private static int selectSourceIndex(List<Chapter> chapters, int minTopics, int maxChapters) {
        int undersizedIndex = -1;
        int undersizedSize = Integer.MAX_VALUE;
        for (int i = 0; i < chapters.size(); i++) {
            int size = topicCount(chapters.get(i));
            if (size < minTopics && size < undersizedSize) {
                undersizedIndex = i;
                undersizedSize = size;
            }
        }
        if (undersizedIndex >= 0) {
            return undersizedIndex;
        }

        if (chapters.size() <= maxChapters) {
            throw new IllegalStateException("No chapter selected for merge despite merge being required");
        }

        int smallestIndex = 0;
        int smallestSize = topicCount(chapters.get(0));
        for (int i = 1; i < chapters.size(); i++) {
            int size = topicCount(chapters.get(i));
            if (size < smallestSize) {
                smallestIndex = i;
                smallestSize = size;
            }
        }
        return smallestIndex;
    }

    private static int selectTargetIndex(List<Chapter> chapters, int sourceIndex) {
        Integer left = sourceIndex > 0 ? sourceIndex - 1 : null;
        Integer right = sourceIndex < chapters.size() - 1 ? sourceIndex + 1 : null;
        if (left == null && right == null) {
            throw new IllegalStateException("Cannot merge a single chapter with itself");
        }
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }

        Chapter source = chapters.get(sourceIndex);
        Chapter leftChapter = chapters.get(left);
        Chapter rightChapter = chapters.get(right);

        int leftAffinity = crossLabelAffinity(source, leftChapter);
        int rightAffinity = crossLabelAffinity(source, rightChapter);
        if (leftAffinity > rightAffinity) {
            return left;
        }
        if (rightAffinity > leftAffinity) {
            return right;
        }

        int leftTopics = topicCount(leftChapter);
        int rightTopics = topicCount(rightChapter);
        if (leftTopics < rightTopics) {
            return left;
        }
        if (rightTopics < leftTopics) {
            return right;
        }

        return left;
    }

    private static int crossLabelAffinity(Chapter a, Chapter b) {
        Set<Long> left = new HashSet<>(Optional.ofNullable(a.getLabels()).orElse(List.of()));
        Set<Long> right = new HashSet<>(Optional.ofNullable(b.getLabels()).orElse(List.of()));
        left.retainAll(right);
        return left.size();
    }

    private static List<Chapter> mergeAt(List<Chapter> chapters, int sourceIndex, int targetIndex) {
        int earlier = Math.min(sourceIndex, targetIndex);
        int later = Math.max(sourceIndex, targetIndex);
        Chapter destination = chapters.get(targetIndex);
        Chapter earlierChapter = chapters.get(earlier);
        Chapter laterChapter = chapters.get(later);

        List<Long> mergedLabels = new ArrayList<>();
        mergedLabels.addAll(Optional.ofNullable(earlierChapter.getLabels()).orElse(List.of()));
        mergedLabels.addAll(Optional.ofNullable(laterChapter.getLabels()).orElse(List.of()));

        List<Long> mergedTopics = new ArrayList<>();
        mergedTopics.addAll(Optional.ofNullable(earlierChapter.getTopics()).orElse(List.of()));
        mergedTopics.addAll(Optional.ofNullable(laterChapter.getTopics()).orElse(List.of()));

        Chapter merged = new Chapter(
                destination.getIndex(),
                destination.getTitle(),
                destination.getDescription(),
                mergedLabels,
                mergedTopics);

        List<Chapter> result = new ArrayList<>(chapters.size() - 1);
        for (int i = 0; i < chapters.size(); i++) {
            if (i == earlier) {
                result.add(merged);
            } else if (i != later) {
                result.add(chapters.get(i));
            }
        }
        return result;
    }

    private static List<Chapter> dropEmpty(List<Chapter> chapters) {
        List<Chapter> result = new ArrayList<>();
        for (Chapter chapter : chapters) {
            if (topicCount(chapter) > 0) {
                result.add(chapter);
            }
        }
        return result;
    }

    private static List<Chapter> reindex(List<Chapter> chapters) {
        List<Chapter> result = new ArrayList<>(chapters.size());
        for (int i = 0; i < chapters.size(); i++) {
            Chapter chapter = chapters.get(i);
            result.add(new Chapter(
                    i, chapter.getTitle(), chapter.getDescription(), chapter.getLabels(), chapter.getTopics()));
        }
        return result;
    }

    private static int topicCount(Chapter chapter) {
        List<Long> topics = chapter.getTopics();
        return topics == null ? 0 : topics.size();
    }
}
