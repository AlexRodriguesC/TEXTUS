package ch.supsi.documentapplication.unit.service.clients.exercise

import ch.supsi.documentapplication.model.AiUsage
import ch.supsi.documentapplication.model.Attempt
import ch.supsi.documentapplication.model.Feedback
import ch.supsi.documentapplication.model.learn.ExerciseType
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.model.learn.exercise.Question
import ch.supsi.documentapplication.service.RetryTemplateWrapper
import ch.supsi.documentapplication.service.clients.exercise.OpenAIExerciseFeedbackClient
import ch.supsi.documentapplication.unit.service.clients.ClientTestSupport
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.retry.RetryTemplate
import spock.lang.Specification
import spock.lang.Subject
import tools.jackson.databind.ObjectMapper

import java.time.Duration

class OpenAIExerciseFeedbackClientSpec extends Specification {

    @Subject
    OpenAIExerciseFeedbackClient client

    ChatClient chatClient = Mock()
    ChatClient.ChatClientRequestSpec requestSpec = Mock()
    ChatClient.CallResponseSpec callResponseSpec = Mock()
    RetryTemplateWrapper retryTemplate
    PromptTemplate promptTemplate = Mock()
    ObjectMapper objectMapper = new ObjectMapper()

    def setup() {
        def retryPolicy = RetryPolicy.builder()
                .maxRetries(2)
                .delay(Duration.ofMillis(10))
                .build()
        retryTemplate = new RetryTemplateWrapper(new RetryTemplate(retryPolicy))

        requestSpec.options(_) >> requestSpec

        client = new OpenAIExerciseFeedbackClient(
                chatClient,
                retryTemplate,
                "",
                promptTemplate,
                ClientTestSupport.TEST_AI_CONTEXT)
    }

    def "should call chatClient and return feedback text when attempt is provided"() {
        given:
        def exercise = new Question()
        exercise.setRequest("What is 2+2?")
        exercise.setAnswer("4")
        exercise.setExplanation("Addition")
        exercise.setDistractor(["3", "5"])
        def topic = new Topic(1L, "a.pdf", 1, 1, "Math", "Math summary", ["addition"], [ExerciseType.QUESTION])
        def attempt = Attempt.of(exercise, "3", topic)
        def feedback = objectMapper.readValue('{"feedback":"Review basic addition."}', Feedback)

        promptTemplate.render(_ as Map) >> "rendered prompt"
        chatClient.prompt(_ as Prompt) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.responseEntity(Feedback.class) >> ClientTestSupport.responseEntityFor(feedback)

        when:
        def result = client.getResult(attempt)

        then:
        result.result() == "Review basic addition."
        result.usages() == [new AiUsage(10, 20, "test-provider", "test-model")]
    }
}
