package ch.supsi.documentapplication.unit.service

import ch.supsi.documentapplication.model.RawModel
import ch.supsi.documentapplication.service.InferenceService
import ch.supsi.documentapplication.service.LLMRestClient
import spock.lang.Specification

class InferenceServiceSpec extends Specification {

    def llmRestClient = Mock(LLMRestClient)
    def service = new InferenceService(llmRestClient)

    def "should delegate to LLMRestClient for available models"() {
        given:
        def models = [Mock(RawModel)]

        when:
        def result = service.getAvailableModels()

        then:
        1 * llmRestClient.getModels() >> models
        result == models
    }
}
