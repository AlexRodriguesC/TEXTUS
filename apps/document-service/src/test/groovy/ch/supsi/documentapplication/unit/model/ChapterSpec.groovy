package ch.supsi.documentapplication.unit.model

import ch.supsi.documentapplication.model.learn.Chapter
import ch.supsi.documentapplication.model.learn.LessonType
import spock.lang.Specification

class ChapterSpec extends Specification {

    def "should build lessons with checkpoint for three topics"() {
        given:
        def chapter = new Chapter(0, "Intro", "desc", [1L, 2L, 3L], [1L, 2L, 3L])

        when:
        chapter.buildLessons()

        then:
        chapter.lessons.size() == 2
        chapter.lessons[0].type == LessonType.LESSON
        chapter.lessons[0].topics == [1L, 2L, 3L]
        chapter.lessons[1].type == LessonType.CHECKPOINT
    }

    def "should distribute six topics across two lesson groups"() {
        given:
        def chapter = new Chapter(0, "Intro", "desc", [1L, 2L, 3L, 4L, 5L, 6L], [1L, 2L, 3L, 4L, 5L, 6L])

        when:
        chapter.buildLessons()

        then:
        chapter.lessons.size() == 3
        chapter.lessons[0].topics.size() == 3
        chapter.lessons[1].topics.size() == 3
        chapter.lessons[2].type == LessonType.CHECKPOINT
    }

    def "should throw when chapter has no topics"() {
        given:
        def chapter = new Chapter(1L, 0, "Intro", "desc", [])

        when:
        chapter.buildLessons()

        then:
        thrown(IllegalStateException)
    }
}
