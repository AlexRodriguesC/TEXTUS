package ch.supsi.textusbackend.unit.controller

import ch.supsi.textusbackend.controller.BookController
import ch.supsi.textusbackend.service.IBookService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification
import spock.lang.Unroll

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest
class BookControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @SpringBean
    IBookService bookService = Mock()

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
        1 * bookService.createBook(_)
    }

    def "should return bad request when file is empty"() {
        given:
        def file = new MockMultipartFile("book", "empty.pdf", MediaType.APPLICATION_PDF_VALUE, new byte[0])

        when:
        def result = mockMvc.perform(multipart("/api/v1/books").file(file))

        then:
        result.andExpect(status().isBadRequest())
        0 * bookService.createBook(_)
    }

    def "should return bad request when file is too large"() {
        given:
        def bookService = Mock(IBookService)
        def controller = new BookController(bookService)
        def file = Mock(MultipartFile) {
            isEmpty() >> false
            getSize() >> 500L * 1024 * 1024
        }

        when:
        def response = controller.uploadBook(file)

        then:
        response.statusCode.value() == 400
        0 * bookService.createBook(_)
    }
}