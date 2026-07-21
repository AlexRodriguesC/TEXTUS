package ch.supsi.documentapplication.unit.controller

import ch.supsi.documentapplication.controller.LLMController
import ch.supsi.documentapplication.controller.DTO.mapper.ModelMapper
import ch.supsi.documentapplication.controller.DTO.response.ModelsResponseDTO
import ch.supsi.documentapplication.configuration.properties.AppProperties
import ch.supsi.documentapplication.model.Architecture
import ch.supsi.documentapplication.model.Pricing
import ch.supsi.documentapplication.model.Provider
import ch.supsi.documentapplication.model.RawModel
import ch.supsi.documentapplication.service.AuthService
import ch.supsi.documentapplication.service.InferenceService
import ch.supsi.documentapplication.service.document.IDocumentService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(LLMController)
@AutoConfigureMockMvc(addFilters = false)
class LLMControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @SpringBean
    InferenceService inferenceService = Mock()

    @SpringBean
    ModelMapper modelMapper = new ModelMapper()

    @SpringBean
    AuthenticationEntryPoint authenticationEntryPoint = Mock()

    @SpringBean
    IDocumentService documentService = Mock()

    @SpringBean
    AuthService authService = Mock()

    @SpringBean
    AppProperties appProperties = Mock()

    def "should return available models"() {
        given:
        def rawModel = new RawModel(
                "model-1", "Model One", "desc",
                new Architecture(["text"], ["text"], null),
                [new Provider("p1", "", null, "stable")],
                new Pricing("0.01", "0.02", null),
                4096, false, false, false, "stable")
        inferenceService.getAvailableModels() >> [rawModel]

        when:
        def result = mockMvc.perform(get("/api/v1/models"))

        then:
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.models[0].id').value("model-1"))
    }
}
