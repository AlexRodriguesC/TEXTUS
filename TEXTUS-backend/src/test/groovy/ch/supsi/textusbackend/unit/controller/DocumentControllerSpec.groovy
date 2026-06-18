package ch.supsi.textusbackend.unit.controller

import ch.supsi.textusbackend.controller.DocumentController
import ch.supsi.textusbackend.service.IDocumentService
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
    IDocumentService bookService = Mock()

    def "should return accepted for valid file"() {
        given:
        def file = new MockMultipartFile("book",
                "book.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "dummy-content".bytes)

        when:
        def result = mockMvc.perform(multipart("/api/v1/books").file(file))

        then:
        result.andExpect(status().isAccepted())
        1 * bookService.parseDocument(_)
    }

    def "should return bad request when file is empty"() {
        given:
        def file = new MockMultipartFile("book", "empty.pdf", MediaType.APPLICATION_PDF_VALUE, new byte[0])

        when:
        def result = mockMvc.perform(multipart("/api/v1/books").file(file))

        then:
        result.andExpect(status().isBadRequest())
        0 * bookService.parseDocument(_)
    }

    def "should return bad request when file is too large"() {
        given:
        def bookService = Mock(IDocumentService)
        def controller = new DocumentController(bookService)
        def file = Mock(MultipartFile) {
            isEmpty() >> false
            getSize() >> 500L * 1024 * 1024
        }

        when:
        def response = controller.uploadDocument(file)

        then:
        response.statusCode.value() == 400
        0 * bookService.parseDocument(_)
    }
}