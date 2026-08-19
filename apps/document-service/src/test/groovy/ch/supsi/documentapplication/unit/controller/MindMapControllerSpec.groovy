package ch.supsi.documentapplication.unit.controller

import ch.supsi.documentapplication.controller.MindMapController
import ch.supsi.documentapplication.configuration.properties.AppProperties
import ch.supsi.documentapplication.controller.DTO.response.MindMapSummaryDTO
import ch.supsi.documentapplication.exception.NotFoundException
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
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

    def "should accept mind map generation and return summary"() {
        given:
        spaceService.createMindMap("alice", 1L) >> new MindMapSummaryDTO(42L, MindMapStatus.GENERATING)

        when:
        def result = mockMvc.perform(post("/api/v1/spaces/1/mind-maps").principal(authentication))

        then:
        result.andExpect(status().isAccepted())
                .andExpect(jsonPath('$.id').value(42))
                .andExpect(jsonPath('$.status').value("GENERATING"))
    }

    def "should return empty mind map list when none exist"() {
        given:
        spaceService.listMindMaps("alice", 1L) >> []

        when:
        def result = mockMvc.perform(get("/api/v1/spaces/1/mind-maps").principal(authentication))

        then:
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.mindMaps').isArray())
                .andExpect(jsonPath('$.mindMaps').isEmpty())
    }

    def "should return mind map list with status"() {
        given:
        spaceService.listMindMaps("alice", 1L) >> [new MindMapSummaryDTO(7L, MindMapStatus.DONE)]

        when:
        def result = mockMvc.perform(get("/api/v1/spaces/1/mind-maps").principal(authentication))

        then:
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.mindMaps[0].id').value(7))
                .andExpect(jsonPath('$.mindMaps[0].status').value("DONE"))
    }

    def "should return mind map list with FAILED status"() {
        given:
        spaceService.listMindMaps("alice", 1L) >> [new MindMapSummaryDTO(7L, MindMapStatus.FAILED)]

        when:
        def result = mockMvc.perform(get("/api/v1/spaces/1/mind-maps").principal(authentication))

        then:
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.mindMaps[0].status').value("FAILED"))
    }

    def "should return mind map by id with status and nodes"() {
        given:
        def mindMap = new MindMap()
        mindMap.id = 7L
        mindMap.jobId = 42L
        mindMap.root = MindMapNode.page(1L)
        spaceService.getMindMap("alice", 1L, 7L) >> mindMap
        spaceService.getMindMapStatus("alice", 1L) >> MindMapStatus.DONE

        when:
        def result = mockMvc.perform(get("/api/v1/spaces/1/mind-maps/7").principal(authentication))

        then:
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.id').value(7))
                .andExpect(jsonPath('$.status').value("DONE"))
                .andExpect(jsonPath('$.nodes').isArray())
    }

    def "should return FAILED status on mind map detail"() {
        given:
        def mindMap = new MindMap()
        mindMap.id = 7L
        mindMap.jobId = 42L
        spaceService.getMindMap("alice", 1L, 7L) >> mindMap
        spaceService.getMindMapStatus("alice", 1L) >> MindMapStatus.FAILED

        when:
        def result = mockMvc.perform(get("/api/v1/spaces/1/mind-maps/7").principal(authentication))

        then:
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.status').value("FAILED"))
    }

    def "should delete mind map by id"() {
        when:
        def result = mockMvc.perform(delete("/api/v1/spaces/1/mind-maps/7").principal(authentication))

        then:
        1 * spaceService.deleteMindMap("alice", 1L, 7L)
        result.andExpect(status().isNoContent())
    }

    def "should return 404 when deleting mind map that does not belong to space"() {
        given:
        spaceService.deleteMindMap("alice", 2L, 7L) >> { throw NotFoundException.mindMapNotFoundException(2L) }

        when:
        def result = mockMvc.perform(delete("/api/v1/spaces/2/mind-maps/7").principal(authentication))

        then:
        result.andExpect(status().isNotFound())
    }
}
