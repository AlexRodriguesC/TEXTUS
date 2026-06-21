package ch.supsi.documentapplication.unit.controller

import ch.supsi.documentapplication.controller.DocumentController
import ch.supsi.documentapplication.properties.AppProperties
import ch.supsi.documentapplication.service.IDocumentService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest
class DocumentControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @SpringBean
    IDocumentService documentService = Mock()

    @SpringBean
    AppProperties appProperties = Mock() {
        getMaxDocumentSize() >> 500L * 1024 * 1024
    }

    def "should return accepted for valid file"() {
        given:
        def file = new MockMultipartFile("document",
                "document.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "dummy-content".bytes)

        when:
        def result = mockMvc.perform(multipart("/api/v1/documents").file(file))

        then:
        result.andExpect(status().isAccepted())
        1 * documentService.parseDocument(_)
    }

    def "should return bad request when file is empty"() {
        given:
        def file = new MockMultipartFile("document", "empty.pdf", MediaType.APPLICATION_PDF_VALUE, new byte[0])

        when:
        def result = mockMvc.perform(multipart("/api/v1/documents").file(file))

        then:
        result.andExpect(status().isBadRequest())
        0 * documentService.parseDocument(_)
    }

    def "should return bad request when file is too large"() {
        given:
        def maxSize = 500L * 1024 * 1024
        def documentService = Mock(IDocumentService)
        def appProps = Mock(AppProperties) {
            it.maxDocumentSize >> maxSize
        }
        def controller = new DocumentController(documentService, appProps)
        def file = Mock(MultipartFile) {
            isEmpty() >> false
            getSize() >> maxSize
        }

        when:
        def response = controller.uploadDocument(file)

        then:
        response.statusCode.value() == 400
        0 * documentService.parseDocument(_)
    }
}