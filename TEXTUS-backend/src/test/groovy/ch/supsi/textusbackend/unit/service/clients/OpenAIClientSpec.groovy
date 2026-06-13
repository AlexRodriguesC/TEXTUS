package ch.supsi.textusbackend.unit.service.clients

import ch.supsi.textusbackend.exception.PromptTemplateLoadException
import ch.supsi.textusbackend.service.clients.OpenAIClient
import org.springframework.ai.chat.client.ChatClient
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class OpenAIClientSpec extends Specification {

    def "should load resource content as string"() {
        given:
        def client = new TestOpenAIClient(Mock(ChatClient))
        def resource = new ByteArrayResource("hello prompt".getBytes(StandardCharsets.UTF_8)) {
            @Override
            String getDescription() {
                return "test-resource"
            }
        }

        when:
        def result = client.loadResource(resource)

        then:
        result == "hello prompt"
    }

    def "should wrap IOException in PromptTemplateLoadException"() {
        given:
        def client = new TestOpenAIClient(Mock(ChatClient))
        def resource = Mock(Resource)
        resource.getInputStream() >> { throw new IOException("boom") }
        resource.getDescription() >> "broken-resource"

        when:
        client.loadResource(resource)

        then:
        def ex = thrown(PromptTemplateLoadException)
        ex.message == "Failed to load prompt template: broken-resource"
        ex.cause instanceof IOException
        ex.cause.message == "boom"
    }

    private static class TestOpenAIClient extends OpenAIClient {
        TestOpenAIClient(ChatClient chatClient) {
            super(chatClient)
        }

        String loadResource(Resource resource) {
            return load(resource)
        }
    }
}