package ch.supsi.documentapplication.unit.controller

import ch.supsi.documentapplication.controller.SpaceController
import ch.supsi.documentapplication.model.Space
import ch.supsi.documentapplication.properties.AppProperties
import ch.supsi.documentapplication.service.AuthService
import ch.supsi.documentapplication.service.ISpaceService
import ch.supsi.documentapplication.service.document.IDocumentService
import com.fasterxml.jackson.databind.ObjectMapper
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.core.Authentication
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
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
}