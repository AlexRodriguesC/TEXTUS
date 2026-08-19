package ch.supsi.documentapplication.integration.dataaccess

import ch.supsi.documentapplication.dataaccess.DatabaseChatStorage
import spock.lang.Subject

class DatabaseChatStorageSpec extends DatabaseSpec {

    @Subject
    DatabaseChatStorage storage

    def setup() {
        storage = new DatabaseChatStorage(jdbcClient)
    }

    def "should create chat and find it by space and owner"() {
        given:
        createUser("chat-owner")
        Long spaceId = createSpace("chat-owner")

        when:
        UUID chatId = storage.create(spaceId, "chat-owner", "First chat")
        def chats = storage.findBySpaceAndOwner(spaceId, "chat-owner")

        then:
        chatId != null
        chats.size() == 1
        chats[0].id() == chatId
        chats[0].title() == "First chat"
        chats[0].spaceId() == spaceId
        chats[0].owner() == "chat-owner"
    }

    def "should find chat by id after creation"() {
        given:
        createUser("chat-owner-2")
        Long spaceId = createSpace("chat-owner-2")
        UUID chatId = storage.create(spaceId, "chat-owner-2", "Lookup chat")

        when:
        def chat = storage.findById(chatId, spaceId, "chat-owner-2")

        then:
        chat.isPresent()
        chat.get().id() == chatId
        chat.get().title() == "Lookup chat"
    }

    def "should delete chat when delete is called"() {
        given:
        createUser("chat-owner-4")
        Long spaceId = createSpace("chat-owner-4")
        UUID chatId = storage.create(spaceId, "chat-owner-4", "Delete me")

        when:
        storage.delete(chatId)
        def chat = storage.findById(chatId, spaceId, "chat-owner-4")

        then:
        chat.isEmpty()
    }
}
