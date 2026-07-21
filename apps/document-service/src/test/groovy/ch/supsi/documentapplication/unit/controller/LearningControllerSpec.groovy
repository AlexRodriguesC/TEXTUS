package ch.supsi.documentapplication.unit.controller

import ch.supsi.documentapplication.controller.LearningController
import ch.supsi.documentapplication.controller.DTO.response.AttemptRequestDTO
import ch.supsi.documentapplication.controller.DTO.response.AttemptResponseDTO
import ch.supsi.documentapplication.controller.DTO.response.LessonResponseDTO
import ch.supsi.documentapplication.controller.DTO.response.exercise.ExerciseMapper
import ch.supsi.documentapplication.controller.DTO.response.exercise.QuestionResponseDTO
import ch.supsi.documentapplication.configuration.properties.AppProperties
import ch.supsi.documentapplication.model.learn.Lesson
import ch.supsi.documentapplication.model.learn.LessonType
import ch.supsi.documentapplication.model.learn.exercise.Question
import ch.supsi.documentapplication.service.AuthService
import ch.supsi.documentapplication.service.ILearningService
import ch.supsi.documentapplication.service.document.IDocumentService
import com.fasterxml.jackson.databind.ObjectMapper
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(LearningController)
@AutoConfigureMockMvc(addFilters = false)
class LearningControllerSpec extends Specification {

    private static final ObjectMapper MAPPER = new ObjectMapper()

    @Autowired
    MockMvc mockMvc

    @SpringBean
    ILearningService learningService = Mock()

    @SpringBean
    ExerciseMapper exerciseMapper = Mock()

    @SpringBean
    AuthenticationEntryPoint authenticationEntryPoint = Mock()

    @SpringBean
    IDocumentService documentService = Mock()

    @SpringBean
    AuthService authService = Mock()

    @SpringBean
    Authentication authentication = Mock() {
        getName() >> "alice"
    }

    @SpringBean
    AppProperties appProperties = Mock()

    def lesson() {
        new Lesson(1L, 1L, 0, LessonType.LESSON, [10L], 0L, 5, 0.10d)
    }

    def "should start lesson"() {
        given:
        learningService.getLesson("alice", 1L, 1L) >> lesson()

        when:
        def result = mockMvc.perform(post("/api/v1/spaces/1/lessons/1").principal(authentication))

        then:
        result.andExpect(status().isCreated())
    }

    def "should get lesson"() {
        given:
        learningService.getLesson("alice", 1L, 1L) >> lesson()

        when:
        def result = mockMvc.perform(get("/api/v1/spaces/1/lessons/1").principal(authentication))

        then:
        result.andExpect(status().isOk())
    }

    def "should get next exercise"() {
        given:
        def exercise = new Question()
        exercise.setId(42L)
        def dto = MAPPER.readValue(
                '{"id":42,"topicId":10,"request":"Q?","completed":false,"type":"QUESTION","answer":"4","distractors":["3"]}',
                QuestionResponseDTO)
        learningService.getNextExercise("alice", 1L, 1L) >> exercise
        exerciseMapper.toDto(exercise) >> dto

        when:
        def result = mockMvc.perform(get("/api/v1/spaces/1/lessons/1/exercise").principal(authentication))

        then:
        result.andExpect(status().isOk())
    }

    def "should list exercises"() {
        given:
        learningService.getExercises("alice", 1L, 1L) >> []

        when:
        def result = mockMvc.perform(get("/api/v1/spaces/1/lessons/1/exercises").principal(authentication))

        then:
        result.andExpect(status().isOk())
    }

    def "should delete exercises"() {
        when:
        def result = mockMvc.perform(delete("/api/v1/spaces/1/lessons/1/exercises").principal(authentication))

        then:
        1 * learningService.deleteExercises("alice", 1L, 1L)
        result.andExpect(status().isNoContent())
    }

    def "should submit attempt"() {
        given:
        def body = '{"exerciseType":"QUESTION","attemptId":"a1","exerciseId":42,"response":"4"}'
        def attemptResponse = MAPPER.readValue(
                '{"exerciseId":42,"correct":true,"lesson":{"id":1,"chapterId":1,"index":0,"type":"LESSON","completed":false,"currentTopic":0,"topics":[10],"mastery":0.1,"maxAttempts":5,"attemptsLeft":5}}',
                AttemptResponseDTO)
        learningService.submitAttempt("alice", 1L, 1L, _ as AttemptRequestDTO) >> attemptResponse

        when:
        def result = mockMvc.perform(
                post("/api/v1/spaces/1/lessons/1/exercise/attempts")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))

        then:
        result.andExpect(status().isCreated())
    }
}
