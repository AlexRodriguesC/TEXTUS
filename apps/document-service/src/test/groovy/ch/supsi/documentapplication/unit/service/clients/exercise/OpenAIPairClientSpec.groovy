package ch.supsi.documentapplication.unit.service.clients.exercise

import ch.supsi.documentapplication.model.AiUsage
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.model.learn.exercise.LeftRight
import ch.supsi.documentapplication.model.learn.exercise.PairExercise
import ch.supsi.documentapplication.service.RetryTemplateWrapper
import ch.supsi.documentapplication.service.clients.exercise.OpenAIPairClient
import ch.supsi.documentapplication.unit.service.clients.ClientTestSupport
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.retry.RetryTemplate
import spock.lang.Specification
import spock.lang.Subject

import java.time.Duration

class OpenAIPairClientSpec extends Specification {

    @Subject
    OpenAIPairClient client

    ChatClient chatClient = Mock()
    ChatClient.ChatClientRequestSpec requestSpec = Mock()
    ChatClient.CallResponseSpec callResponseSpec = Mock()
    RetryTemplateWrapper retryTemplate
    PromptTemplate promptTemplate = Mock()

    def setup() {
        def retryPolicy = RetryPolicy.builder()
                .maxRetries(2)
                .delay(Duration.ofMillis(10))
                .build()
        retryTemplate = new RetryTemplateWrapper(new RetryTemplate(retryPolicy))

        client = new OpenAIPairClient(
                chatClient,
                retryTemplate,
                "",
                promptTemplate,
                ClientTestSupport.TEST_AI_CONTEXT)
    }

    def "should call chatClient and return the generated pair exercise when topic is provided"() {
        given:
        def topic = new Topic(1L, "a.pdf", 1, "Animals", "Animal sounds summary", ["sounds"])
        def expectedPair = new PairExercise()
        expectedPair.setRequest("Match animal to sound")
        expectedPair.setAnswer([new LeftRight("cat", "meow"), new LeftRight("dog", "bark")])
        expectedPair.setExplanation("Common animal sounds")

        promptTemplate.render(_ as Map) >> "rendered prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.responseEntity(PairExercise.class) >> ClientTestSupport.responseEntityFor(expectedPair)

        when:
        def result = client.getResult(topic)

        then:
        result.result() == expectedPair
        result.usages() == [new AiUsage(10, 20, "test-provider", "test-model")]
    }
}
