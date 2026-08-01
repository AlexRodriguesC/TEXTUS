package ch.supsi.documentapplication.unit.service.clients.exercise

import ch.supsi.documentapplication.model.AiUsage
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.service.RetryTemplateWrapper
import ch.supsi.documentapplication.service.clients.EvaluationRestClient
import ch.supsi.documentapplication.service.clients.exercise.OpenAIListClient
import ch.supsi.documentapplication.unit.service.clients.ClientTestSupport
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.retry.RetryTemplate
import spock.lang.Specification
import spock.lang.Subject

import java.time.Duration

class OpenAIListClientSpec extends Specification {

    @Subject
    OpenAIListClient client

    ChatClient chatClient = Mock()
    ChatClient.ChatClientRequestSpec requestSpec = Mock()
    ChatClient.CallResponseSpec callResponseSpec = Mock()
    RetryTemplateWrapper retryTemplate
    PromptTemplate promptTemplate = Mock()
    EvaluationRestClient evaluationRestClient = Mock()

    def setup() {
        def retryPolicy = RetryPolicy.builder()
                .maxRetries(2)
                .delay(Duration.ofMillis(10))
                .build()
        retryTemplate = new RetryTemplateWrapper(new RetryTemplate(retryPolicy))

        requestSpec.options(_) >> requestSpec

        client = new OpenAIListClient(
                chatClient,
                retryTemplate,
                "",
                promptTemplate,
                ClientTestSupport.TEST_AI_CONTEXT,
                evaluationRestClient)
    }

    def "should call chatClient and return the generated list exercise when topic is provided"() {
        given:
        def topic = new Topic(1L, "a.pdf", 1, 1, "Processes", "Process summary", ["steps"])
        def expectedList = new ch.supsi.documentapplication.model.learn.exercise.List()
        expectedList.setRequest("Order these steps")
        expectedList.setAnswer(["step1", "step2", "step3"])
        expectedList.setExplanation("Correct workflow order")

        promptTemplate.render(_ as Map) >> "rendered prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.responseEntity(ch.supsi.documentapplication.model.learn.exercise.List.class) >>
                ClientTestSupport.responseEntityFor(expectedList)

        when:
        def result = client.getResult(topic)

        then:
        result.result() == expectedList
        result.usages() == [new AiUsage(10, 20, "test-provider", "test-model")]
        1 * evaluationRestClient.evaluateExercise(topic, expectedList)
    }
}
