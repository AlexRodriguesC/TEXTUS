package ch.supsi.documentapplication.unit.controller

import ch.supsi.documentapplication.controller.MindMapController
import ch.supsi.documentapplication.configuration.properties.AppProperties
import ch.supsi.documentapplication.model.MindMap
import ch.supsi.documentapplication.model.MindMapNode
import ch.supsi.documentapplication.model.MindMapStatus
import ch.supsi.documentapplication.service.AuthService
import ch.supsi.documentapplication.service.ISpaceService
import ch.supsi.documentapplication.service.document.IDocumentService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.security.core.Authentication
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(MindMapController)
@AutoConfigureMockMvc(addFilters = false)
class MindMapControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @SpringBean
    ISpaceService spaceService = Mock()

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

    def "should accept mind map generation"() {
        when:
        def result = mockMvc.perform(post("/api/v1/spaces/1/mind-maps").principal(authentication))

        then:
        1 * spaceService.createMindMap("alice", 1L)
        result.andExpect(status().isNoContent())
    }

    def "should return mind map job status"() {
        given:
        spaceService.getMindMapStatus("alice", 1L) >> MindMapStatus.DONE

        when:
        def result = mockMvc.perform(get("/api/v1/spaces/1/mind-maps/jobs").principal(authentication))

        then:
        result.andExpect(status().isOk())
    }

    def "should return mind map"() {
        given:
        def mindMap = new MindMap()
        mindMap.root = new MindMapNode(1L)
        spaceService.getMindMap("alice", 1L) >> mindMap

        when:
        def result = mockMvc.perform(get("/api/v1/spaces/1/mind-maps").principal(authentication))

        then:
        result.andExpect(status().isOk())
    }
}
