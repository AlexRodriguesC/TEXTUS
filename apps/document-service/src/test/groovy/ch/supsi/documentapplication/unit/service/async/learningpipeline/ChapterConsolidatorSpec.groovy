package ch.supsi.documentapplication.unit.service.async.learningpipeline

import ch.supsi.documentapplication.model.learn.Chapter
import ch.supsi.documentapplication.service.async.learningpipeline.ChapterConsolidator
import spock.lang.Specification
import spock.lang.Subject

class ChapterConsolidatorSpec extends Specification {

    @Subject
    ChapterConsolidator consolidator = new ChapterConsolidator()

    def "should merge undersized adjacent chapters into valid groups"() {
        given:
        def chapters = [
                chapter(0, "One", "First chapter focus", [1L], [10L, 11L]),
                chapter(1, "Two", "Second chapter focus", [2L], [20L]),
                chapter(2, "Three", "Third chapter focus", [3L], [30L, 31L, 32L])
        ]

        when:
        def result = consolidator.consolidate(chapters, 10)

        then:
        result.size() == 2
        result*.index == [0, 1]
        result[0].topics == [10L, 11L, 20L]
        result[0].title == "One"
        result[1].topics == [30L, 31L, 32L]
        result.every { it.topics.size() >= 3 }
    }

    def "should prefer the adjacent chapter with the strongest cross-label affinity"() {
        given:
        def chapters = [
                chapter(0, "Left", "Left side focus", [1L, 9L], [10L, 11L, 12L]),
                chapter(1, "Middle", "Middle side focus", [2L, 9L], [20L]),
                chapter(2, "Right", "Right side focus", [3L], [30L, 31L, 32L])
        ]

        when:
        def result = consolidator.consolidate(chapters, 10)

        then:
        result.size() == 2
        result[0].title == "Left"
        result[0].topics == [10L, 11L, 12L, 20L]
        result[0].labels == [1L, 9L, 2L, 9L]
        result[1].title == "Right"
    }

    def "should break affinity ties by preferring the smaller then earlier neighbor"() {
        given:
        def chapters = [
                chapter(0, "Left", "Left side focus", [1L], [10L, 11L, 12L, 13L]),
                chapter(1, "Middle", "Middle side focus", [2L], [20L]),
                chapter(2, "Right", "Right side focus", [3L], [30L, 31L])
        ]

        when:
        def result = consolidator.consolidate(chapters, 10)

        then:
        result.size() == 2
        result[0].title == "Left"
        result[0].topics == [10L, 11L, 12L, 13L]
        result[1].title == "Right"
        result[1].topics == [20L, 30L, 31L]
    }

    def "should reduce to the absolute cap when all chapters already have three topics"() {
        given:
        def chapters = [
                chapter(0, "A", "Chapter a focus here", [1L], [10L, 11L, 12L]),
                chapter(1, "B", "Chapter b focus here", [2L], [20L, 21L, 22L]),
                chapter(2, "C", "Chapter c focus here", [3L], [30L, 31L, 32L]),
                chapter(3, "D", "Chapter d focus here", [4L], [40L, 41L, 42L])
        ]

        when:
        def result = consolidator.consolidate(chapters, 2)

        then:
        result.size() == 2
        result*.index == [0, 1]
        result[0].topics == [10L, 11L, 12L, 20L, 21L, 22L]
        result[1].topics == [30L, 31L, 32L, 40L, 41L, 42L]
        result.every { it.topics.size() >= 3 }
    }

    def "should allow one chapter when fewer than three topics exist overall"() {
        given:
        def chapters = [
                chapter(0, "Only", "Single chapter focus", [1L], [10L]),
                chapter(1, "Tiny", "Tiny chapter focus", [2L], [20L])
        ]

        when:
        def result = consolidator.consolidate(chapters, 1)

        then:
        result.size() == 1
        result[0].index == 0
        result[0].topics == [10L, 20L]
    }

    def "should preserve stable topic and label ordering with contiguous indices"() {
        given:
        def chapters = [
                chapter(5, "Alpha", "Alpha chapter focus", [1L, 2L], [10L, 11L]),
                chapter(8, "Beta", "Beta chapter focus", [3L], [20L]),
                chapter(9, "Gamma", "Gamma chapter focus", [4L, 5L], [30L, 31L, 32L])
        ]

        when:
        def result = consolidator.consolidate(chapters, 10)

        then:
        result*.index == [0, 1]
        result[0].labels == [1L, 2L, 3L]
        result[0].topics == [10L, 11L, 20L]
        result[1].labels == [4L, 5L]
        result[1].topics == [30L, 31L, 32L]
    }

    def "should drop empty chapters before consolidating"() {
        given:
        def chapters = [
                chapter(0, "Empty", "Empty chapter focus", [1L], []),
                chapter(1, "Keep", "Keep chapter focus", [2L], [10L, 11L, 12L])
        ]

        when:
        def result = consolidator.consolidate(chapters, 10)

        then:
        result.size() == 1
        result[0].index == 0
        result[0].title == "Keep"
        result[0].topics == [10L, 11L, 12L]
    }

    private static Chapter chapter(int index, String title, String description, List<Long> labels, List<Long> topics) {
        new Chapter(index, title, description, labels, topics)
    }
}
