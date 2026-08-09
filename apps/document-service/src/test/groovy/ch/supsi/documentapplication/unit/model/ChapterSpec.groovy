package ch.supsi.documentapplication.unit.model

import ch.supsi.documentapplication.model.learn.Chapter
import ch.supsi.documentapplication.model.learn.LessonType
import spock.lang.Specification
import tools.jackson.databind.ObjectMapper

class ChapterSpec extends Specification {

    def "should build lessons with checkpoint for three topics"() {
        given:
        def chapter = new Chapter(0, "Intro", "desc", [1L, 2L, 3L], null)

        when:
        chapter.buildLessons()

        then:
        chapter.lessons.size() == 2
        chapter.lessons[0].type == LessonType.LESSON
        chapter.lessons[0].topics == [1L, 2L, 3L]
        chapter.lessons[1].type == LessonType.CHECKPOINT
    }

    def "should distribute six topics across two contiguous lesson groups"() {
        given:
        def chapter = new Chapter(0, "Intro", "desc", [1L, 2L, 3L, 4L, 5L, 6L], null)

        when:
        chapter.buildLessons()

        then:
        chapter.lessons.size() == 3
        chapter.lessons[0].topics == [1L, 2L, 3L]
        chapter.lessons[1].topics == [4L, 5L, 6L]
        chapter.lessons[2].type == LessonType.CHECKPOINT
    }

    def "should keep ten topics contiguous with remainder on earlier lessons"() {
        given:
        def chapter = new Chapter(
                0,
                "Intro",
                "desc",
                [1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L],
                null)

        when:
        chapter.buildLessons()

        then:
        chapter.lessons.size() == 4
        chapter.lessons[0].topics == [1L, 2L, 3L, 4L]
        chapter.lessons[1].topics == [5L, 6L, 7L]
        chapter.lessons[2].topics == [8L, 9L, 10L]
        chapter.lessons[3].type == LessonType.CHECKPOINT
    }

    def "should throw when chapter has no topics"() {
        given:
        def chapter = new Chapter(1L, 0, "Intro", "desc", [])

        when:
        chapter.buildLessons()

        then:
        thrown(IllegalStateException)
    }

    def "should exclude document provenance from JSON"() {
        given:
        def chapter = new Chapter(0, "Intro", "desc", [10L], 99L)

        when:
        def json = new ObjectMapper().writeValueAsString(chapter)

        then:
        !json.contains("documentId")
        !json.contains("topics")
        !json.contains("labels")
        json.contains('"index":0')
        json.contains('"title":"Intro"')
    }
}
