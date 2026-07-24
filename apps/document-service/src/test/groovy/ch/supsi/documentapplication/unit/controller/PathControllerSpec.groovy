package ch.supsi.documentapplication.unit.controller

import ch.supsi.documentapplication.controller.PathController
import ch.supsi.documentapplication.configuration.properties.AppProperties
import ch.supsi.documentapplication.exception.NotFoundException
import ch.supsi.documentapplication.model.PathSettings
import ch.supsi.documentapplication.model.learn.LearningPath
import ch.supsi.documentapplication.model.learn.LearningPathStatus
import ch.supsi.documentapplication.service.AuthService
import ch.supsi.documentapplication.service.IPathService
import ch.supsi.documentapplication.service.document.IDocumentService
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(PathController)
@AutoConfigureMockMvc(addFilters = false)
class PathControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @SpringBean
    IPathService pathService = Mock()

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

    def "should accept learning path generation"() {
        when:
        def result = mockMvc.perform(post("/api/v1/spaces/1/paths").principal(authentication))

        then:
        1 * pathService.createLearningPath("alice", 1L)
        result.andExpect(status().isAccepted())
    }

    def "should return learning path when present"() {
        given:
        pathService.getLearningPath("alice", 1L) >> new LearningPath(99L, new PathSettings(25, 25, 25, 25), [])
        pathService.getLearningPathStatus("alice", 1L) >> LearningPathStatus.DONE

        when:
        def result = mockMvc.perform(get("/api/v1/spaces/1/paths").principal(authentication))

        then:
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.paths[0].status').value('DONE'))
    }

    def "should return empty list when learning path is absent"() {
        given:
        pathService.getLearningPath("alice", 1L) >> null

        when:
        def result = mockMvc.perform(get("/api/v1/spaces/1/paths").principal(authentication))

        then:
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.paths').isArray())
                .andExpect(jsonPath('$.paths').isEmpty())
        0 * pathService.getLearningPathStatus(_, _)
    }

    def "should update path settings"() {
        given:
        def body = '{"questionPercentage":25,"listPercentage":25,"groupPercentage":25,"pairPercentage":25}'

        when:
        def result = mockMvc.perform(
                put("/api/v1/spaces/1/paths/settings")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))

        then:
        1 * pathService.updateSettings("alice", 1L, _ as PathSettings)
        result.andExpect(status().isOk())
    }

    def "should delete learning path"() {
        when:
        def result = mockMvc.perform(delete("/api/v1/spaces/1/paths").principal(authentication))

        then:
        1 * pathService.deleteLearningPath("alice", 1L)
        result.andExpect(status().isNoContent())
    }
}
