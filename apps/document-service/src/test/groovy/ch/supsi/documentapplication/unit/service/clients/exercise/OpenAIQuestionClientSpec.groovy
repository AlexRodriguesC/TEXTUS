package ch.supsi.documentapplication.unit.service.clients.exercise

import ch.supsi.documentapplication.model.AiUsage
import ch.supsi.documentapplication.model.learn.ExerciseType
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.model.learn.exercise.Question
import ch.supsi.documentapplication.service.RetryTemplateWrapper
import ch.supsi.documentapplication.service.clients.exercise.OpenAIQuestionClient
import ch.supsi.documentapplication.unit.service.clients.ClientTestSupport
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.retry.RetryTemplate
import spock.lang.Specification
import spock.lang.Subject

import java.time.Duration

class OpenAIQuestionClientSpec extends Specification {

    @Subject
    OpenAIQuestionClient client

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

        requestSpec.options(_) >> requestSpec

        client = new OpenAIQuestionClient(
                chatClient,
                retryTemplate,
                "",
                promptTemplate,
                ClientTestSupport.TEST_AI_CONTEXT)
    }

    def "should call chatClient and return the generated question exercise when topic is provided"() {
        given:
        def topic = new Topic(1L, "a.pdf", 1, 1, "Algebra", "Basic algebra summary", ["equations"], [ExerciseType.QUESTION])
        def expectedQuestion = new Question()
        expectedQuestion.setRequest("What is 2+2?")
        expectedQuestion.setAnswer("4")
        expectedQuestion.setExplanation("Simple addition")
        expectedQuestion.setDistractor(["3", "5"])

        promptTemplate.render(_ as Map) >> "rendered prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.responseEntity(Question.class) >> ClientTestSupport.responseEntityFor(expectedQuestion)

        when:
        def result = client.getResult(topic)

        then:
        result.result() == expectedQuestion
        result.usages() == [new AiUsage(10, 20, "test-provider", "test-model")]
    }
}
