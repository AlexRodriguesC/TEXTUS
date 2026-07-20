package ch.supsi.documentapplication.unit.service

import ch.supsi.documentapplication.controller.DTO.response.ChatMessageResponseDTO
import ch.supsi.documentapplication.dataaccess.IDatabaseChatStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage
import ch.supsi.documentapplication.exception.UnauthorizedException
import ch.supsi.documentapplication.model.Chat
import ch.supsi.documentapplication.model.Space
import ch.supsi.documentapplication.service.ChatService
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.messages.UserMessage
import spock.lang.Specification

import java.time.Instant
import java.util.Optional
import java.util.UUID

class ChatServiceSpec extends Specification {

    IDatabaseSpaceStorage spaceStorage = Mock()
    IDatabaseChatStorage chatStorage = Mock()
    ChatClient chatClient = Mock()
    ChatMemory chatMemory = Mock()

    ChatService service = new ChatService(spaceStorage, chatStorage, chatClient, chatMemory, "default-model")

    def "listChats validates space ownership"() {
        given:
        spaceStorage.read(1L) >> Optional.of(new Space(1L, "pippo", "icon", "Space", "desc"))

        when:
        service.listChats("pippo", 1L)

        then:
        1 * chatStorage.findBySpaceAndOwner(1L, "pippo") >> []
    }

    def "listChats throws when space not owned"() {
        given:
        spaceStorage.read(1L) >> Optional.of(new Space(1L, "other", "icon", "Space", "desc"))

        when:
        service.listChats("pippo", 1L)

        then:
        thrown(UnauthorizedException)
        0 * chatStorage._
    }

    def "deleteChat clears memory and removes metadata"() {
        given:
        def chatId = UUID.randomUUID()
        def now = Instant.now()
        def chat = new Chat(chatId, 1L, "pippo", "Title", now, now)
        spaceStorage.read(1L) >> Optional.of(new Space(1L, "pippo", "icon", "Space", "desc"))
        chatStorage.findById(chatId, 1L, "pippo") >> Optional.of(chat)

        when:
        service.deleteChat("pippo", 1L, chatId)

        then:
        1 * chatMemory.clear(chatId.toString())
        1 * chatStorage.delete(chatId)
    }

    def "getHistory maps user messages"() {
        given:
        def chatId = UUID.randomUUID()
        def now = Instant.now()
        def chat = new Chat(chatId, 1L, "pippo", "Title", now, now)
        spaceStorage.read(1L) >> Optional.of(new Space(1L, "pippo", "icon", "Space", "desc"))
        chatStorage.findById(chatId, 1L, "pippo") >> Optional.of(chat)
        chatMemory.get(chatId.toString()) >> [new UserMessage("Hello")]

        when:
        List<ChatMessageResponseDTO> history = service.getHistory("pippo", 1L, chatId)

        then:
        history.size() == 1
        history[0].role() == "user"
        history[0].content() == "Hello"
    }
}
