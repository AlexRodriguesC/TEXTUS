package ch.supsi.documentapplication.unit.controller

import ch.supsi.documentapplication.controller.AuthController
import ch.supsi.documentapplication.configuration.properties.AppProperties
import ch.supsi.documentapplication.model.User
import ch.supsi.documentapplication.service.AuthService
import ch.supsi.documentapplication.service.document.IDocumentService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AuthController)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @SpringBean
    AuthenticationManager authenticationManager = Mock()

    @SpringBean
    AuthService authService = Mock()

    @SpringBean
    AuthenticationEntryPoint authenticationEntryPoint = Mock()

    @SpringBean
    IDocumentService documentService = Mock()

    @SpringBean
    AppProperties appProperties = Mock()

    def "should register user and return token"() {
        given:
        def body = '{"username":"alice","password":"secret"}'

        when:
        def result = mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))

        then:
        1 * authService.register(_ as User) >> "token-123"
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.token').value("token-123"))
    }

    def "should login and return token"() {
        given:
        def body = '{"username":"alice","password":"secret"}'

        when:
        def result = mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))

        then:
        1 * authenticationManager.authenticate(_ as UsernamePasswordAuthenticationToken)
        1 * authService.generateToken("alice") >> "token-456"
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.token').value("token-456"))
    }

    def "should return 401 when login credentials are invalid"() {
        given:
        def body = '{"username":"alice","password":"wrong"}'

        when:
        def result = mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))

        then:
        1 * authenticationManager.authenticate(_ as UsernamePasswordAuthenticationToken) >> {
            throw new BadCredentialsException("bad")
        }
        0 * authService.generateToken(_)
        result.andExpect(status().isUnauthorized())
    }
}
