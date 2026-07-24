package ch.supsi.documentapplication.unit.service

import ch.supsi.documentapplication.controller.DTO.response.ChatMessageResponseDTO
import ch.supsi.documentapplication.dataaccess.IDatabaseChatStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage
import ch.supsi.documentapplication.exception.UnauthorizedException
import ch.supsi.documentapplication.model.Chat
import ch.supsi.documentapplication.model.Space
import ch.supsi.documentapplication.service.ChatService
import ch.supsi.documentapplication.service.rag.ChatRagAdvisorFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.api.Advisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.ai.openai.OpenAiChatOptions
import reactor.core.publisher.Flux
import spock.lang.Specification

import java.time.Instant
import java.util.Optional
import java.util.UUID

class ChatServiceSpec extends Specification {

    IDatabaseSpaceStorage spaceStorage = Mock()
    IDatabaseChatStorage chatStorage = Mock()
    ChatClient chatClient = Mock()
    ChatMemory chatMemory = Mock()
    ChatRagAdvisorFactory chatRagAdvisorFactory = Mock()
    Advisor ragAdvisor = Mock()

    ChatService service = new ChatService(spaceStorage, chatStorage, chatClient, chatMemory, chatRagAdvisorFactory, "default-model")

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

    def "streamMessage validates space and attaches RAG advisor with history"() {
        given:
        def chatId = UUID.randomUUID()
        def now = Instant.now()
        def chat = new Chat(chatId, 1L, "pippo", "Title", now, now)
        spaceStorage.read(1L) >> Optional.of(new Space(1L, "pippo", "icon", "Space", "desc"))
        chatStorage.findById(chatId, 1L, "pippo") >> Optional.of(chat)
        chatMemory.get(chatId.toString()) >> [new UserMessage("Earlier question")]

        def requestSpec = Mock(ChatClient.ChatClientRequestSpec)
        def streamSpec = Mock(ChatClient.StreamResponseSpec)

        when:
        Flux<String> flux = service.streamMessage("pippo", 1L, chatId, "Follow up", null)

        then:
        1 * chatRagAdvisorFactory.createForSpace(1L) >> ragAdvisor
        1 * chatClient.prompt() >> requestSpec
        1 * requestSpec.messages(_) >> requestSpec
        1 * requestSpec.user("Follow up") >> requestSpec
        1 * requestSpec.advisors(ragAdvisor) >> requestSpec
        1 * requestSpec.advisors(_) >> requestSpec
        1 * requestSpec.options(_ as ChatOptions.Builder) >> { ChatOptions.Builder builder ->
            assert builder instanceof OpenAiChatOptions.Builder
            assert ((OpenAiChatOptions.Builder) builder).build().model == "default-model"
            requestSpec
        }
        1 * requestSpec.stream() >> streamSpec
        1 * streamSpec.content() >> Flux.just("answer")
        flux.collectList().block() == ["answer"]
    }
}
