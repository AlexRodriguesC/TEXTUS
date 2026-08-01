package ch.supsi.documentapplication.unit.controller

import ch.supsi.documentapplication.controller.TopicController
import ch.supsi.documentapplication.configuration.properties.AppProperties
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.service.AuthService
import ch.supsi.documentapplication.service.ITopicService
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(TopicController)
@AutoConfigureMockMvc(addFilters = false)
class TopicControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @SpringBean
    ITopicService topicService = Mock()

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

    def "should return topics for space"() {
        given:
        topicService.getTopicsBySpace("alice", 1L) >> [
                new Topic(10L, "doc.pdf", 1, 1, "Title", "Summary", ["label"])
        ]

        when:
        def result = mockMvc.perform(get("/api/v1/spaces/1/topics").principal(authentication))

        then:
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.topics[0].id').value(10))
        result.andExpect(jsonPath('$.topics[0].page').value(1))
        result.andExpect(jsonPath('$.topics[0].title').value("Title"))
    }
}
