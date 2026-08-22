package ch.supsi.documentapplication.unit.service

import ch.supsi.documentapplication.controller.DTO.response.ChatMessageResponseDTO
import ch.supsi.documentapplication.dataaccess.IDatabaseChatMessageTopicStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseChatStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage
import ch.supsi.documentapplication.exception.UnauthorizedException
import ch.supsi.documentapplication.model.Chat
import ch.supsi.documentapplication.model.SequencedChatMessage
import ch.supsi.documentapplication.model.Space
import ch.supsi.documentapplication.model.learn.ExerciseType
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.repository.ISequencedChatMessageRepository
import ch.supsi.documentapplication.service.ChatService
import ch.supsi.documentapplication.service.rag.ChatRagAdvisorFactory
import ch.supsi.documentapplication.service.rag.FallbackTopicDocumentPostProcessor
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.ChatClientResponse
import org.springframework.ai.chat.client.advisor.api.Advisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.Generation
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.ai.document.Document
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor
import reactor.core.publisher.Flux
import spock.lang.Specification

import java.time.Instant
import java.util.Optional
import java.util.UUID

class ChatServiceSpec extends Specification {

    IDatabaseSpaceStorage spaceStorage = Mock()
    IDatabaseChatStorage chatStorage = Mock()
    IDatabaseChatMessageTopicStorage chatMessageTopicStorage = Mock()
    IDatabaseTopicStorage topicStorage = Mock()
    ISequencedChatMessageRepository sequencedChatMessageRepository = Mock()
    ChatClient chatClient = Mock()
    ChatMemory chatMemory = Mock()
    ChatRagAdvisorFactory chatRagAdvisorFactory = Mock()
    Advisor ragAdvisor = Mock()

    ChatService service = new ChatService(
            spaceStorage,
            chatStorage,
            chatMessageTopicStorage,
            topicStorage,
            sequencedChatMessageRepository,
            chatClient,
            chatMemory,
            chatRagAdvisorFactory,
            "default-model")

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

    def "getHistory maps user messages with empty topics"() {
        given:
        def chatId = UUID.randomUUID()
        def now = Instant.now()
        def chat = new Chat(chatId, 1L, "pippo", "Title", now, now)
        spaceStorage.read(1L) >> Optional.of(new Space(1L, "pippo", "icon", "Space", "desc"))
        chatStorage.findById(chatId, 1L, "pippo") >> Optional.of(chat)
        sequencedChatMessageRepository.findByConversationId(chatId.toString()) >> [
                new SequencedChatMessage(new UserMessage("Hello"), 0L, now)
        ]

        when:
        List<ChatMessageResponseDTO> history = service.getHistory("pippo", 1L, chatId)

        then:
        1 * chatMessageTopicStorage.findByChatId(chatId) >> [:]
        history.size() == 1
        history[0].role() == "user"
        history[0].content() == "Hello"
        history[0].topics() == []
    }

    def "getHistory maps assistant topics by sequence id"() {
        given:
        def chatId = UUID.randomUUID()
        def now = Instant.parse("2026-01-01T00:00:00Z")
        def chat = new Chat(chatId, 1L, "pippo", "Title", now, now)
        def assistant = AssistantMessage.builder().content("Answer").build()
        spaceStorage.read(1L) >> Optional.of(new Space(1L, "pippo", "icon", "Space", "desc"))
        chatStorage.findById(chatId, 1L, "pippo") >> Optional.of(chat)
        sequencedChatMessageRepository.findByConversationId(chatId.toString()) >> [
                new SequencedChatMessage(assistant, 1L, now)
        ]

        when:
        List<ChatMessageResponseDTO> history = service.getHistory("pippo", 1L, chatId)

        then:
        1 * chatMessageTopicStorage.findByChatId(chatId) >> [(1L): [12L, 45L]]
        history.size() == 1
        history[0].role() == "assistant"
        history[0].topics() == [12L, 45L]
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
        def clientResponse = ChatClientResponse.builder()
                .chatResponse(ChatResponse.builder()
                        .generations([new Generation(new AssistantMessage("answer"))])
                        .build())
                .context([:])
                .build()

        when:
        def result = service.streamMessage("pippo", 1L, chatId, "Follow up", null, null).collectList().block()

        then:
        1 * chatRagAdvisorFactory.createForSpace(1L, []) >> ragAdvisor
        0 * topicStorage._
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
        1 * streamSpec.chatClientResponse() >> Flux.just(clientResponse)
        1 * chatStorage.touchUpdatedAt(chatId)
        0 * chatMessageTopicStorage._
        result == ["answer"]
    }

    def "streamMessage pins validated topic ids on the RAG advisor"() {
        given:
        def chatId = UUID.randomUUID()
        def now = Instant.now()
        def chat = new Chat(chatId, 1L, "pippo", "Title", now, now)
        spaceStorage.read(1L) >> Optional.of(new Space(1L, "pippo", "icon", "Space", "desc"))
        chatStorage.findById(chatId, 1L, "pippo") >> Optional.of(chat)
        chatMemory.get(chatId.toString()) >> []
        def pinnedTopic = new Topic(12L, null, 1L, 1, "Pinned", "Summary", [], [ExerciseType.QUESTION])

        def requestSpec = Mock(ChatClient.ChatClientRequestSpec)
        def streamSpec = Mock(ChatClient.StreamResponseSpec)
        def clientResponse = ChatClientResponse.builder()
                .chatResponse(ChatResponse.builder()
                        .generations([new Generation(new AssistantMessage("answer"))])
                        .build())
                .context([:])
                .build()

        when:
        service.streamMessage("pippo", 1L, chatId, "Help me", null, [12L, 12L, 999L, null]).collectList().block()

        then:
        1 * topicStorage.getByIds(1L, [12L, 999L]) >> [pinnedTopic]
        1 * chatRagAdvisorFactory.createForSpace(1L, [12L]) >> ragAdvisor
        1 * chatClient.prompt() >> requestSpec
        1 * requestSpec.messages(_) >> requestSpec
        1 * requestSpec.user("Help me") >> requestSpec
        1 * requestSpec.advisors(ragAdvisor) >> requestSpec
        1 * requestSpec.advisors(_) >> requestSpec
        1 * requestSpec.options(_ as ChatOptions.Builder) >> requestSpec
        1 * requestSpec.stream() >> streamSpec
        1 * streamSpec.chatClientResponse() >> Flux.just(clientResponse)
        1 * chatStorage.touchUpdatedAt(chatId)
    }

    def "streamMessage persists grounded topic ids for assistant reply after stream completes"() {
        given:
        def chatId = UUID.randomUUID()
        def now = Instant.parse("2026-01-02T00:00:00Z")
        def chat = new Chat(chatId, 1L, "pippo", "Title", now, now)
        def assistant = AssistantMessage.builder().content("answer").build()
        spaceStorage.read(1L) >> Optional.of(new Space(1L, "pippo", "icon", "Space", "desc"))
        chatStorage.findById(chatId, 1L, "pippo") >> Optional.of(chat)
        chatMemory.get(chatId.toString()) >> [new UserMessage("Earlier question")]

        def requestSpec = Mock(ChatClient.ChatClientRequestSpec)
        def streamSpec = Mock(ChatClient.StreamResponseSpec)
        def document = Document.builder()
                .text("topic")
                .metadata([(FallbackTopicDocumentPostProcessor.METADATA_TOPIC_ID): 99L])
                .build()
        def clientResponse = ChatClientResponse.builder()
                .chatResponse(ChatResponse.builder()
                        .generations([new Generation(new AssistantMessage("answer"))])
                        .build())
                .context([(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT): [document]])
                .build()

        when:
        service.streamMessage("pippo", 1L, chatId, "Follow up", null, null).blockLast()

        then:
        1 * chatRagAdvisorFactory.createForSpace(1L, []) >> ragAdvisor
        1 * chatClient.prompt() >> requestSpec
        1 * requestSpec.messages(_) >> requestSpec
        1 * requestSpec.user("Follow up") >> requestSpec
        1 * requestSpec.advisors(ragAdvisor) >> requestSpec
        1 * requestSpec.advisors(_) >> requestSpec
        1 * requestSpec.options(_ as ChatOptions.Builder) >> requestSpec
        1 * requestSpec.stream() >> streamSpec
        1 * streamSpec.chatClientResponse() >> Flux.just(clientResponse)
        1 * sequencedChatMessageRepository.findByConversationId(chatId.toString()) >> [
                new SequencedChatMessage(new UserMessage("Earlier question"), 0L, now.minusSeconds(60)),
                new SequencedChatMessage(assistant, 1L, now)
        ]
        1 * chatStorage.touchUpdatedAt(chatId)
        1 * chatMessageTopicStorage.save(chatId, 1L, [99L])
    }

    def "streamMessage does not persist grounding when assistant message is not yet available"() {
        given:
        def chatId = UUID.randomUUID()
        def now = Instant.parse("2026-01-02T00:00:00Z")
        def chat = new Chat(chatId, 1L, "pippo", "Title", now, now)
        spaceStorage.read(1L) >> Optional.of(new Space(1L, "pippo", "icon", "Space", "desc"))
        chatStorage.findById(chatId, 1L, "pippo") >> Optional.of(chat)
        chatMemory.get(chatId.toString()) >> [new UserMessage("Earlier question")]

        def requestSpec = Mock(ChatClient.ChatClientRequestSpec)
        def streamSpec = Mock(ChatClient.StreamResponseSpec)
        def document = Document.builder()
                .text("topic")
                .metadata([(FallbackTopicDocumentPostProcessor.METADATA_TOPIC_ID): 99L])
                .build()
        def clientResponse = ChatClientResponse.builder()
                .chatResponse(ChatResponse.builder()
                        .generations([new Generation(new AssistantMessage("answer"))])
                        .build())
                .context([(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT): [document]])
                .build()

        when:
        service.streamMessage("pippo", 1L, chatId, "Follow up", null, null).blockLast()

        then:
        1 * chatRagAdvisorFactory.createForSpace(1L, []) >> ragAdvisor
        1 * chatClient.prompt() >> requestSpec
        1 * requestSpec.messages(_) >> requestSpec
        1 * requestSpec.user("Follow up") >> requestSpec
        1 * requestSpec.advisors(ragAdvisor) >> requestSpec
        1 * requestSpec.advisors(_) >> requestSpec
        1 * requestSpec.options(_ as ChatOptions.Builder) >> requestSpec
        1 * requestSpec.stream() >> streamSpec
        1 * streamSpec.chatClientResponse() >> Flux.just(clientResponse)
        1 * sequencedChatMessageRepository.findByConversationId(chatId.toString()) >> [
                new SequencedChatMessage(new UserMessage("Earlier question"), 0L, now.minusSeconds(60)),
                new SequencedChatMessage(new UserMessage("Follow up"), 1L, now)
        ]
        1 * chatStorage.touchUpdatedAt(chatId)
        0 * chatMessageTopicStorage.save(_, _, _)
    }

    def "streamMessage persists grounding only for latest assistant message"() {
        given:
        def chatId = UUID.randomUUID()
        def older = Instant.parse("2026-01-01T00:00:00Z")
        def newer = Instant.parse("2026-01-02T00:00:00Z")
        def chat = new Chat(chatId, 1L, "pippo", "Title", newer, newer)
        def olderAssistant = AssistantMessage.builder().content("first answer").build()
        def newerAssistant = AssistantMessage.builder().content("second answer").build()
        spaceStorage.read(1L) >> Optional.of(new Space(1L, "pippo", "icon", "Space", "desc"))
        chatStorage.findById(chatId, 1L, "pippo") >> Optional.of(chat)
        chatMemory.get(chatId.toString()) >> [new UserMessage("Earlier question")]

        def requestSpec = Mock(ChatClient.ChatClientRequestSpec)
        def streamSpec = Mock(ChatClient.StreamResponseSpec)
        def document = Document.builder()
                .text("topic")
                .metadata([(FallbackTopicDocumentPostProcessor.METADATA_TOPIC_ID): 99L])
                .build()
        def clientResponse = ChatClientResponse.builder()
                .chatResponse(ChatResponse.builder()
                        .generations([new Generation(new AssistantMessage("second answer"))])
                        .build())
                .context([(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT): [document]])
                .build()

        when:
        service.streamMessage("pippo", 1L, chatId, "Follow up", null, null).blockLast()

        then:
        1 * chatRagAdvisorFactory.createForSpace(1L, []) >> ragAdvisor
        1 * chatClient.prompt() >> requestSpec
        1 * requestSpec.messages(_) >> requestSpec
        1 * requestSpec.user("Follow up") >> requestSpec
        1 * requestSpec.advisors(ragAdvisor) >> requestSpec
        1 * requestSpec.advisors(_) >> requestSpec
        1 * requestSpec.options(_ as ChatOptions.Builder) >> requestSpec
        1 * requestSpec.stream() >> streamSpec
        1 * streamSpec.chatClientResponse() >> Flux.just(clientResponse)
        1 * sequencedChatMessageRepository.findByConversationId(chatId.toString()) >> [
                new SequencedChatMessage(olderAssistant, 0L, older),
                new SequencedChatMessage(newerAssistant, 3L, newer)
        ]
        1 * chatStorage.touchUpdatedAt(chatId)
        1 * chatMessageTopicStorage.save(chatId, 3L, [99L])
        0 * chatMessageTopicStorage.save(chatId, 0L, _)
    }
}
