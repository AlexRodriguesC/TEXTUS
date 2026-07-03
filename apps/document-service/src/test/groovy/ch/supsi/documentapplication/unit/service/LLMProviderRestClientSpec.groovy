package ch.supsi.documentapplication.unit.service


import ch.supsi.documentapplication.model.Architecture
import ch.supsi.documentapplication.model.RawModel
import ch.supsi.documentapplication.service.LLMProviderRestClient
import org.springframework.web.client.RestClient
import spock.lang.Specification
import spock.lang.Subject

import java.util.function.Function
import java.util.function.Predicate

class LLMProviderRestClientSpec extends Specification {

    RestClient restClient = Mock(RestClient)
    RestClient.RequestHeadersUriSpec headersUriSpec = Mock(RestClient.RequestHeadersUriSpec)
    RestClient.RequestHeadersSpec headersSpec = Mock(RestClient.RequestHeadersSpec)
    RestClient.ResponseSpec responseSpec = Mock(RestClient.ResponseSpec)

    @Subject
    LLMProviderRestClient client = new LLMProviderRestClient(restClient)

    def setup() {
        restClient.get() >> headersUriSpec
        headersUriSpec.uri(_ as Function) >> headersSpec
        headersSpec.retrieve() >> responseSpec
    }

    RawModel model(String id = "abc", String name, String description = "def", Architecture architecture = null, def providers = [], def pricing = null, int contextLength = 10, boolean jsonOutput = true, boolean structuredOutputs = false, boolean free = true, String stability = "stable"){
        return new RawModel(id, name, description, architecture, providers, pricing, contextLength, jsonOutput, structuredOutputs, free, stability);
    }

    def "should return list of models when response contains data"() {
        given:
        responseSpec.onStatus(_ as Predicate, _ as RestClient.ResponseSpec.ErrorHandler) >> responseSpec
        RawModel model1 = model("gpt-4")
        RawModel model2 = model("gpt-3.5")
        def response = new LLMProviderRestClient.RawModelListResponse([model1, model2])
        responseSpec.body(LLMProviderRestClient.RawModelListResponse.class) >> response

        when:
        def result = client.getModels()

        then:
        result == [model1, model2]
    }

    def "should return empty list when response body is null"() {
        given:
        responseSpec.onStatus(_ as Predicate, _ as RestClient.ResponseSpec.ErrorHandler) >> responseSpec
        responseSpec.body(LLMProviderRestClient.RawModelListResponse.class) >> null

        when:
        def result = client.getModels()

        then:
        result == []
    }

    def "should return empty list when response data is null"() {
        given:
        responseSpec.onStatus(_ as Predicate, _ as RestClient.ResponseSpec.ErrorHandler) >> responseSpec
        def response = new LLMProviderRestClient.RawModelListResponse(null)
        responseSpec.body(LLMProviderRestClient.RawModelListResponse.class) >> response

        when:
        def result = client.getModels()

        then:
        result == []
    }
}