package ch.supsi.documentapplication.integration.dataaccess

import ch.supsi.documentapplication.dataaccess.DatabasePathStorage
import ch.supsi.documentapplication.model.PathSettings
import ch.supsi.documentapplication.model.learn.Chapter
import ch.supsi.documentapplication.model.learn.Lesson
import ch.supsi.documentapplication.model.learn.LessonType
import spock.lang.Subject

class DatabasePathStorageSpec extends DatabaseSpec {

    @Subject
    DatabasePathStorage storage

    def setup() {
        storage = new DatabasePathStorage(jdbcClient)
    }

    def "should create learning path and read it back with default settings"() {
        given:
        createUser()
        Long spaceId = createSpace()
        Long jobId = createBatchJobInstance()

        when:
        Long pathId = storage.createLearningPath(spaceId, jobId)
        def learningPath = storage.read(spaceId)

        then:
        pathId != null
        learningPath != null
        learningPath.jobId == jobId
        learningPath.settings.questionPercentage != null
        learningPath.chapters.isEmpty()
    }

    def "should create chapter and read it back in round trip"() {
        given:
        createUser()
        Long spaceId = createSpace()
        Long jobId = createBatchJobInstance()
        Long pathId = storage.createLearningPath(spaceId, jobId)
        Long topicId = createTopic(spaceId, jobId)
        def lesson = new Lesson(LessonType.LESSON, 0, [topicId])
        def chapter = new Chapter(0, "Chapter 1", "First chapter", List.of())
        chapter.setLessons([lesson])

        when:
        storage.create(pathId, chapter, spaceId)
        def learningPath = storage.read(spaceId)

        then:
        learningPath.chapters.size() == 1
        learningPath.chapters[0].title == "Chapter 1"
        learningPath.chapters[0].description == "First chapter"
        learningPath.chapters[0].lessons.size() == 1
        learningPath.chapters[0].lessons[0].topics == [topicId]
    }

    def "should update settings and read them back"() {
        given:
        createUser()
        Long spaceId = createSpace()
        Long jobId = createBatchJobInstance()
        storage.createLearningPath(spaceId, jobId)
        def settings = new PathSettings(50, 20, 20, 10)

        when:
        storage.updateSettings(spaceId, settings)
        def learningPath = storage.read(spaceId)

        then:
        learningPath.settings.questionPercentage == 50
        learningPath.settings.listPercentage == 20
        learningPath.settings.groupPercentage == 20
        learningPath.settings.pairPercentage == 10
    }

    def "should delete learning path when delete is called"() {
        given:
        createUser()
        Long spaceId = createSpace()
        Long jobId = createBatchJobInstance()
        storage.createLearningPath(spaceId, jobId)

        when:
        storage.delete(spaceId)
        def learningPath = storage.read(spaceId)

        then:
        learningPath == null
    }
}
