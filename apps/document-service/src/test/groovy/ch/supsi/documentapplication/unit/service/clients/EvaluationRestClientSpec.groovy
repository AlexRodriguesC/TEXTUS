package ch.supsi.documentapplication.unit.service.clients

import ch.supsi.documentapplication.configuration.properties.EvalServiceProperties
import ch.supsi.documentapplication.model.evaluation.CreateEvaluationRequest
import ch.supsi.documentapplication.model.evaluation.EvalMetricName
import ch.supsi.documentapplication.model.evaluation.MetricConfig
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.service.clients.EvaluationRestClient
import org.springframework.web.client.RestClient
import spock.lang.Specification
import spock.lang.Subject

class EvaluationRestClientSpec extends Specification {

    RestClient restClient = Mock()
    RestClient.RequestBodyUriSpec bodyUriSpec = Mock()
    RestClient.RequestBodySpec bodySpec = Mock()
    RestClient.ResponseSpec responseSpec = Mock()
    EvalServiceProperties evalServiceProperties = new EvalServiceProperties()

    @Subject
    EvaluationRestClient client

    def setup() {
        evalServiceProperties.evaluationsEndpoint = "/api/v1/evaluations"
        client = new EvaluationRestClient(restClient, evalServiceProperties)

        restClient.post() >> bodyUriSpec
        bodyUriSpec.uri(_ as String) >> bodySpec
        bodySpec.body(_ as CreateEvaluationRequest) >> bodySpec
        bodySpec.retrieve() >> responseSpec
        responseSpec.toBodilessEntity() >> null
    }

    def "should POST evaluation request to configured endpoint"() {
        given:
        def request = CreateEvaluationRequest.of(
                "topic_list",
                "summary text",
                "1. Topic A",
                [MetricConfig.of(EvalMetricName.RELEVANCE)])

        when:
        client.submit(request)

        then:
        1 * restClient.post() >> bodyUriSpec
        1 * bodyUriSpec.uri("/api/v1/evaluations") >> bodySpec
        1 * bodySpec.body({ CreateEvaluationRequest body ->
            body.contentType() == "topic_list" &&
                    body.input() == "summary text" &&
                    body.actualOutput() == "1. Topic A" &&
                    body.metrics().size() == 1 &&
                    body.metrics()[0].name() == "relevance"
        }) >> bodySpec
        1 * bodySpec.retrieve() >> responseSpec
        1 * responseSpec.toBodilessEntity()
    }

    def "should not propagate exceptions when evaluation service is unreachable"() {
        given:
        def request = CreateEvaluationRequest.of(
                "topic",
                "summary",
                "topic text",
                [MetricConfig.of(EvalMetricName.HALLUCINATION)])
        responseSpec.toBodilessEntity() >> { throw new RuntimeException("connection refused") }

        when:
        client.submit(request)

        then:
        noExceptionThrown()
    }

    def "should submit topic list and hallucination evaluations"() {
        given:
        def topics = [new Topic(1L, "file", 1, 1, "Topic A", "Summary A", ["a"])]
        def clientSpy = Spy(client)

        when:
        clientSpy.evaluateTopicList("page summary", topics)
        clientSpy.evaluateTopicHallucination("page summary", topics[0])

        then:
        1 * clientSpy.submitAsync({ CreateEvaluationRequest request ->
            request.contentType() == "topic_list" &&
                    request.input() == "page summary" &&
                    request.actualOutput() == "1. Topic A\nSummary A" &&
                    request.metrics()*.name() == ["relevance"]
        })
        1 * clientSpy.submitAsync({ CreateEvaluationRequest request ->
            request.contentType() == "topic" &&
                    request.input() == "page summary" &&
                    request.actualOutput() == "Topic A\nSummary A" &&
                    request.metrics()*.name() == ["hallucination"]
        })
    }

    def "submitAsync should not throw when called"() {
        when:
        client.submitAsync(CreateEvaluationRequest.of(
                "topic",
                "summary",
                "output",
                [MetricConfig.of(EvalMetricName.HALLUCINATION)]))

        then:
        noExceptionThrown()
    }

    def "should not POST when evaluations are disabled"() {
        given:
        evalServiceProperties.enabled = false
        def request = CreateEvaluationRequest.of(
                "topic_list",
                "summary text",
                "1. Topic A",
                [MetricConfig.of(EvalMetricName.RELEVANCE)])
        def topics = [new Topic(1L, "file", 1, 1, "Topic A", "Summary A", ["a"])]

        when:
        client.submit(request)
        client.submitAsync(request)
        client.evaluateTopicList("page summary", topics)

        then:
        0 * restClient.post()
        noExceptionThrown()
    }
}
