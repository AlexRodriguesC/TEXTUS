package ch.supsi.documentapplication.unit.service.clients.exercise

import ch.supsi.documentapplication.model.AiUsage
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.model.learn.exercise.Group
import ch.supsi.documentapplication.service.RetryTemplateWrapper
import ch.supsi.documentapplication.service.clients.EvaluationRestClient
import ch.supsi.documentapplication.service.clients.exercise.OpenAIGroupClient
import ch.supsi.documentapplication.unit.service.clients.ClientTestSupport
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.retry.RetryTemplate
import spock.lang.Specification
import spock.lang.Subject

import java.time.Duration

class OpenAIGroupClientSpec extends Specification {

    @Subject
    OpenAIGroupClient client

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

        client = new OpenAIGroupClient(
                chatClient,
                retryTemplate,
                "",
                promptTemplate,
                ClientTestSupport.TEST_AI_CONTEXT,
                evaluationRestClient)
    }

    def "should call chatClient and return the generated group exercise when topic is provided"() {
        given:
        def topic = new Topic(1L, "a.pdf", 1, 1, "Biology", "Biology summary", ["taxonomy"])
        def expectedGroup = new Group()
        expectedGroup.setRequest("Group these items")
        expectedGroup.setAnswer([fruit: ["apple", "banana"], vegetable: ["carrot"]])
        expectedGroup.setExplanation("Food categories")

        promptTemplate.render(_ as Map) >> "rendered prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.responseEntity(Group.class) >> ClientTestSupport.responseEntityFor(expectedGroup)

        when:
        def result = client.getResult(topic)

        then:
        result.result() == expectedGroup
        result.usages() == [new AiUsage(10, 20, "test-provider", "test-model")]
        1 * evaluationRestClient.evaluateExercise(topic, expectedGroup)
    }
}
