package ch.supsi.documentapplication.unit.controller

import ch.supsi.documentapplication.configuration.properties.AppProperties
import ch.supsi.documentapplication.controller.ChatController
import ch.supsi.documentapplication.controller.DTO.response.ChatHistoryResponseDTO
import ch.supsi.documentapplication.controller.DTO.response.ChatMessageResponseDTO
import ch.supsi.documentapplication.controller.DTO.response.ChatResponseDTO
import ch.supsi.documentapplication.controller.DTO.response.ChatsResponseDTO
import ch.supsi.documentapplication.model.Chat
import ch.supsi.documentapplication.service.AuthService
import ch.supsi.documentapplication.service.IChatService
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

import java.time.Instant
import java.util.UUID

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ChatController)
@AutoConfigureMockMvc(addFilters = false)
class ChatControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @SpringBean
    IChatService chatService = Mock()

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

    def "should list chats"() {
        given:
        def chatId = UUID.randomUUID()
        def now = Instant.parse("2026-01-01T00:00:00Z")
        def chat = new Chat(chatId, 1L, "pippo", "My chat", now, now)
        chatService.listChats("pippo", 1L) >> [chat]

        when:
        def result = mockMvc.perform(get("/api/v1/spaces/1/chats").principal(authentication))

        then:
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.chats[0].id').value(chatId.toString()))
        result.andExpect(jsonPath('$.chats[0].title').value("My chat"))
    }

    def "should create chat"() {
        given:
        def chatId = UUID.randomUUID()
        def now = Instant.parse("2026-01-01T00:00:00Z")
        def chat = new Chat(chatId, 1L, "pippo", "New chat", now, now)
        chatService.createChat("pippo", 1L, null) >> chat

        when:
        def result = mockMvc.perform(
                post("/api/v1/spaces/1/chats")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON))

        then:
        result.andExpect(status().isCreated())
        result.andExpect(jsonPath('$.id').value(chatId.toString()))
    }

    def "should return chat history"() {
        given:
        def chatId = UUID.randomUUID()
        def timestamp = Instant.parse("2026-01-01T00:00:00Z")
        chatService.getHistory("pippo", 1L, chatId) >> [
                new ChatMessageResponseDTO("user", "Hello", timestamp, List.of())
        ]

        when:
        def result = mockMvc.perform(get("/api/v1/spaces/1/chats/${chatId}").principal(authentication))

        then:
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.messages[0].role').value("user"))
        result.andExpect(jsonPath('$.messages[0].content').value("Hello"))
        result.andExpect(jsonPath('$.messages[0].topics').isArray())
    }

    def "should delete chat"() {
        given:
        def chatId = UUID.randomUUID()

        when:
        def result = mockMvc.perform(delete("/api/v1/spaces/1/chats/${chatId}").principal(authentication))

        then:
        1 * chatService.deleteChat("pippo", 1L, chatId)
        result.andExpect(status().isNoContent())
    }
}
