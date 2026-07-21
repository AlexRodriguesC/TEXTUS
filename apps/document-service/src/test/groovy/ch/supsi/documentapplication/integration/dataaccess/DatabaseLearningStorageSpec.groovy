package ch.supsi.documentapplication.integration.dataaccess


import ch.supsi.documentapplication.dataaccess.DatabaseLearningStorage
import ch.supsi.documentapplication.dataaccess.DatabasePathStorage
import ch.supsi.documentapplication.dataaccess.DatabaseSpaceStorage
import ch.supsi.documentapplication.dataaccess.DatabaseTopicStorage
import ch.supsi.documentapplication.dataaccess.DatabaseUserStorage
import ch.supsi.documentapplication.model.Space
import ch.supsi.documentapplication.model.User
import ch.supsi.documentapplication.model.learn.Chapter
import ch.supsi.documentapplication.model.learn.Lesson
import ch.supsi.documentapplication.model.learn.LessonType
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.model.learn.exercise.Group
import ch.supsi.documentapplication.model.learn.exercise.LeftRight
import ch.supsi.documentapplication.model.learn.exercise.PairExercise
import ch.supsi.documentapplication.model.learn.exercise.Question
import spock.lang.Subject

class DatabaseLearningStorageSpec extends DatabaseSpec {

    @Subject
    DatabaseLearningStorage storage

    DatabasePathStorage pathStorage
    DatabaseSpaceStorage spaceStorage
    DatabaseUserStorage userStorage
    DatabaseTopicStorage topicStorage

    Long lessonId
    Long topicId

    def setup() {
        storage = new DatabaseLearningStorage(jdbcClient)
        pathStorage = new DatabasePathStorage(jdbcClient)
        spaceStorage = new DatabaseSpaceStorage(jdbcClient)
        topicStorage = new DatabaseTopicStorage(jdbcClient)
        userStorage = new DatabaseUserStorage(jdbcClient)

        lessonId = createLesson()
    }

    private Long createLesson() {
        userStorage.createUser(new User("owner", "abc"))
        Space space = new Space("owner", "icon", "name", "description")
        Long spaceId = spaceStorage.create(space)

        Long jobId = createBatchJobInstance()
        Long pathId = pathStorage.createLearningPath(spaceId, jobId)
        Long topicId = createTopic(spaceId, jobId)

        Lesson lesson = new Lesson(LessonType.LESSON, 0, [topicId])

        Chapter chapter = new Chapter(0, "Chapter 1", "desc", List.of())
        chapter.setLessons([lesson])

        pathStorage.create(pathId, chapter, spaceId)

        this.topicId = topicId

        return jdbcClient
                .sql("SELECT id FROM documents.lesson WHERE chapter_id = (SELECT id FROM documents.chapter WHERE path_id = :pathId)")
                .param("pathId", pathId)
                .query(Long.class)
                .single()
    }

    private Long createDocument(Long spaceId, Long jobId) {
        return jdbcClient
                .sql("""
                INSERT INTO documents.document (name, job_id, space_id)
                VALUES ('test-document', :jobId, :spaceId)
                RETURNING id
                """)
                .param("jobId", jobId)
                .param("spaceId", spaceId)
                .query(Long.class)
                .single()
    }

    private Long createPage(Long spaceId, Long jobId) {
        Long documentId = createDocument(spaceId, jobId)

        return jdbcClient
                .sql("""
                INSERT INTO documents.page (space_id, document_id, title, summary)
                VALUES (:spaceId, :documentId, 'test title', 'test summary')
                RETURNING id
                """)
                .param("spaceId", spaceId)
                .param("documentId", documentId)
                .query(Long.class)
                .single()
    }

    private Long createTopic(Long spaceId, Long jobId) {
        Long pageId = createPage(spaceId, jobId)

        Topic topic = new Topic(null, "a", 1, "Topic title", "Topic summary", ["label1", "label2"])
        topicStorage.create(topic, pageId)

        return jdbcClient
                .sql("SELECT id FROM documents.topic WHERE page_id = :pageId")
                .param("pageId", pageId)
                .query(Long.class)
                .single()
    }

    private Long createBatchJobInstance() {
        Long jobInstanceId = jdbcClient
                .sql("SELECT COALESCE(MAX(job_instance_id), 0) + 1 FROM documents.batch_job_instance")
                .query(Long.class)
                .single()

        jdbcClient
                .sql("""
                INSERT INTO documents.batch_job_instance (job_instance_id, version, job_name, job_key)
                VALUES (:jobInstanceId, 0, 'test-job', :jobKey)
                """)
                .param("jobInstanceId", jobInstanceId)
                .param("jobKey", UUID.randomUUID().toString().substring(0, 32))
                .update()

        return jobInstanceId
    }

    def "creates and reads back a question exercise, then updates it"() {
        given:
        Question question = new Question()
        question.setTopicId(1L)
        question.setIndex(0)
        question.setExplanation("Explain me")
        question.setRequest("What is 2+2?")
        question.setAnswer("4")
        question.setDistractor(["3", "5"])

        when:
        storage.createExercise(question, lessonId)
        def exercises = storage.getExercises(lessonId)
        def saved = exercises.find { it.id == question.id } as Question

        then:
        saved.request == "What is 2+2?"
        saved.answer == "4"
        saved.distractor.sort() == ["3", "5"]
        saved.explanation == "Explain me"
        saved.topicId == 1L
        saved.index == 0

        when:
        question.setId(saved.id)
        question.setRequest("What is 3+3?")
        question.setAnswer("6")
        question.setDistractor(["5", "7"])
        question.setExplanation("Updated explanation")
        storage.updateExercise(question)
        def updated = storage.getExercises(lessonId).find { it.id == question.id } as Question

        then:
        updated.request == "What is 3+3?"
        updated.answer == "6"
        updated.distractor.sort() == ["5", "7"]
        updated.explanation == "Updated explanation"
    }

    def "creates and reads back a list exercise, then updates it"() {
        given:
        def list = new ch.supsi.documentapplication.model.learn.exercise.List()
        list.setTopicId(this.topicId)
        list.setIndex(0)
        list.setExplanation("Explain list")
        list.setRequest("Order these steps")
        list.setAnswer(["step1", "step2", "step3"])

        when:
        storage.createExercise(list, lessonId)
        def saved = storage.getExercises(lessonId).find { it.id == list.id }

        then:
        saved.request == "Order these steps"
        saved.answer == ["step1", "step2", "step3"]

        when:
        list.setId(saved.id)
        list.setRequest("Order these new steps")
        list.setAnswer(["a", "b"])
        storage.updateExercise(list)
        def updated = storage.getExercises(lessonId).find { it.id == list.id }

        then:
        updated.request == "Order these new steps"
        updated.answer == ["a", "b"]
    }

    def "creates and reads back a group exercise, then updates it"() {
        given:
        Group group = new Group()
        group.setTopicId(this.topicId)
        group.setIndex(0)
        group.setExplanation("Explain group")
        group.setRequest("Group these items")
        group.setAnswer([fruit: ["apple", "banana"], vegetable: ["carrot"]])

        when:
        storage.createExercise(group, lessonId)
        def saved = storage.getExercises(lessonId).find { it.id == group.id } as Group

        then:
        saved.request == "Group these items"
        saved.answer["fruit"].sort() == ["apple", "banana"]
        saved.answer["vegetable"] == ["carrot"]

        when:
        group.setId(saved.id)
        group.setRequest("Group these new items")
        group.setAnswer([animal: ["dog", "cat"]])
        storage.updateExercise(group)
        def updated = storage.getExercises(lessonId).find { it.id == group.id } as Group

        then:
        updated.request == "Group these new items"
        updated.answer.keySet() == ["animal"] as Set
        updated.answer["animal"].sort() == ["cat", "dog"]
    }

    def "creates and reads back a pair exercise, then updates it"() {
        given:
        PairExercise pair = new PairExercise()
        pair.setTopicId(this.topicId)
        pair.setIndex(0)
        pair.setExplanation("Explain pairs")
        pair.setRequest("Match these pairs")
        pair.setAnswer([new LeftRight("cat", "meow"), new LeftRight("dog", "bark")])

        when:
        storage.createExercise(pair, lessonId)
        def saved = storage.getExercises(lessonId).find { it.id == pair.id } as PairExercise

        then:
        saved.request == "Match these pairs"
        saved.answer.collect { [it.left(), it.right()] }.sort() ==
                [["cat", "meow"], ["dog", "bark"]].sort()

        when:
        pair.setId(saved.id)
        pair.setRequest("Match these new pairs")
        pair.setAnswer([new LeftRight("cow", "moo")])
        storage.updateExercise(pair)
        def updated = storage.getExercises(lessonId).find { it.id == pair.id } as PairExercise

        then:
        updated.request == "Match these new pairs"
        updated.answer.collect { [it.left(), it.right()] } == [["cow", "moo"]]
    }


    def "reads back a lesson with correct base info and no exercises"() {
        when:
        Lesson lesson = storage.readLesson(lessonId)

        then:
        lesson.id == lessonId
        lesson.type == LessonType.LESSON
        lesson.index == 0
        lesson.currentTopic == 0
        lesson.attemptsLeft == lesson.getMaxAttempts()
        lesson.mastery == 0.10d
        lesson.exercisesLength == 0
    }

    def "reads back a lesson including its exercises"() {
        given:
        Question question = new Question()
        question.setTopicId(topicId)
        question.setIndex(0)
        question.setExplanation("Explain me")
        question.setRequest("What is 2+2?")
        question.setAnswer("4")
        question.setDistractor(["3", "5"])
        storage.createExercise(question, lessonId)

        when:
        Lesson lesson = storage.readLesson(lessonId)

        then:
        lesson.exercisesLength == 1
        lesson.exercises.find { it.id == question.id } != null
    }

    def "updates a lesson's current topic, attempts left and mastery"() {
        given:
        Lesson lesson = storage.readLesson(lessonId)

        when:
        lesson.setAttemptsLeft(5)
        lesson.setMastery(0.42d)
        storage.update(lesson)
        Lesson updated = storage.readLesson(lessonId)

        then:
        updated.attemptsLeft == 5
        updated.mastery == 0.42d
    }

    def "consumeAttempt reflected after update"() {
        given:
        Lesson lesson = storage.readLesson(lessonId)
        int before = lesson.attemptsLeft

        when:
        lesson.consumeAttempt()
        storage.update(lesson)
        Lesson updated = storage.readLesson(lessonId)

        then:
        updated.attemptsLeft == before - 1
    }
}
