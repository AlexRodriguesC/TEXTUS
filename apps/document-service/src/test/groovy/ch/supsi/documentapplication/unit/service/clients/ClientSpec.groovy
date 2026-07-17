package ch.supsi.documentapplication.unit.service.clients

import ch.supsi.documentapplication.exception.RetriesFailedException
import ch.supsi.documentapplication.service.RetryTemplateWrapper
import ch.supsi.documentapplication.service.clients.Client
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.retry.TransientAiException
import org.springframework.core.retry.RetryException
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.retry.RetryTemplate
import spock.lang.Specification
import spock.lang.Subject

import java.time.Duration

class ClientSpec extends Specification {

    @Subject
    Client<String, String> client

    ChatClient chatClient = Mock()
    PromptTemplate promptTemplate = Mock()
    RetryTemplateWrapper retryTemplate

    def setup() {
        def retryPolicy = RetryPolicy.builder()
                .maxRetries(2)
                .includes(TransientAiException.class)
                .delay(Duration.ofMillis(10))
                .build()

        retryTemplate = new RetryTemplateWrapper(new RetryTemplate(retryPolicy))

        client = new Client<String, String>(chatClient, retryTemplate, promptTemplate) {
            @Override
            protected String call(String input) {
                throw new TransientAiException("abc")
            }
        }
    }

    def "should throw RetriesFailedException when policy is exhausted"() {
        when: "getResult is called and the underlying call always throws TransientAiException"
        client.getResult("input")

        then: "a RetryException is thrown after retries are exhausted"
        thrown(RetriesFailedException)
    }

    def "should build prompt using promptTemplate"() {
        given: "a rendered template and params"
        def params = [key: "value"]
        promptTemplate.render(params) >> "rendered prompt"

        when: "getPrompt is called"
        def prompt = client.getPrompt(params)

        then: "the prompt contains the rendered user message"
        prompt.instructions.size() == 1
        prompt.instructions[0].text == "rendered prompt"
    }
}
