package ch.supsi.documentapplication.unit.controller

import ch.supsi.documentapplication.controller.SpaceController
import ch.supsi.documentapplication.model.Space
import ch.supsi.documentapplication.configuration.properties.AppProperties
import ch.supsi.documentapplication.service.AuthService
import ch.supsi.documentapplication.service.ISpaceService
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(SpaceController)
@AutoConfigureMockMvc(addFilters = false)
class SpaceControllerSpec extends Specification {

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
        getName() >> "pippo"
    }

    @SpringBean
    AppProperties appProperties = Mock() {
        getMaxDocumentSize() >> 500L * 1024 * 1024
    }

    def "should return 200 and space id when space is created"() {
        given:
        def body = new ObjectMapper().writeValueAsString([icon: "abc", name: "My Space", description: "A test space"])
        spaceService.create(_ as Space) >> 42L

        when:
        def result = mockMvc.perform(
                post("/api/v1/spaces")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))

        then:
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.id').value(42))
    }

    def "should list spaces for owner"() {
        given:
        spaceService.getSpaces("pippo") >> [new Space(1L, "pippo", "icon", "Space", "desc")]

        when:
        def result = mockMvc.perform(get("/api/v1/spaces").principal(authentication))

        then:
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$[0].spaceId').value(1))
        result.andExpect(jsonPath('$[0].name').value("Space"))
    }

    def "should delete space"() {
        when:
        def result = mockMvc.perform(delete("/api/v1/spaces/1").principal(authentication))

        then:
        1 * spaceService.delete("pippo", 1L)
        result.andExpect(status().isNoContent())
    }

    def "should update space"() {
        given:
        def body = new ObjectMapper().writeValueAsString([icon: "new", name: "Updated", description: "New desc"])

        when:
        def result = mockMvc.perform(
                patch("/api/v1/spaces/1")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))

        then:
        1 * spaceService.update(_ as Space)
        result.andExpect(status().isNoContent())
    }
}