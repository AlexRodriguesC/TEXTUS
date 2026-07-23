package ch.supsi.documentapplication.unit.controller.DTO.mapper

import ch.supsi.documentapplication.controller.DTO.mapper.ModelMapper
import ch.supsi.documentapplication.controller.DTO.response.ModelResponseDTO
import ch.supsi.documentapplication.controller.DTO.response.ModelsResponseDTO
import ch.supsi.documentapplication.model.Architecture
import ch.supsi.documentapplication.model.Pricing
import ch.supsi.documentapplication.model.Provider
import ch.supsi.documentapplication.model.RawModel
import spock.lang.Specification
import spock.lang.Subject

class ModelMapperSpec extends Specification {

    @Subject
    ModelMapper mapper = new ModelMapper()

    def "should map text-only stable model with valid context length to DTO"() {
        given:
        Architecture architecture = architecture(["text"], ["text"])
        Pricing pricing = pricing("0.01", "0.02")
        Provider provider = new Provider("provider-1", "", null, "stable")
        RawModel rawModel = new RawModel(
                "gpt-4.1-mini", "Model Ten", "desc",
                architecture, [provider], pricing, 4096, false, false, false, "stable"
        )

        when:
        ModelsResponseDTO result = mapper.toDTO([rawModel])

        then:
        result.models().size() == 1
        ModelResponseDTO dto = result.models().first()
        dto.id() == "gpt-4.1-mini"
        dto.name() == "Model Ten"
        dto.description() == "desc"
        dto.promptPrice() == "0.01"
        dto.completionPrice() == "0.02"
        dto.contextLength() == 4096
        dto.stability() == "stable"
        !dto.free()
    }

    def "should resolve stability from provider when model stability is null"() {
        given:
        Architecture architecture = architecture(["text"], ["text"])
        Pricing pricing = pricing("0.01", "0.02")
        Provider provider = new Provider("provider-1", "ab", null, "beta")
        RawModel rawModel = new RawModel(
                "gpt-4.1-mini", "Model Ten", "desc",
                architecture, [provider], pricing, 1024, false, false, false, null
        )

        when:
        ModelsResponseDTO result = mapper.toDTO([rawModel])

        then:
        result.models().first().stability() == "beta"
    }

    def "should resolve stability as unknown when model and provider stability are null"() {
        given:
        Architecture architecture = architecture(["text"], ["text"])
        Pricing pricing = pricing("0.01", "0.02")
        RawModel rawModel = new RawModel(
                "gpt-4.1-mini", "Model Ten", "desc",
                architecture, [], pricing, 1024, false, false, false, null
        )

        when:
        ModelsResponseDTO result = mapper.toDTO([rawModel])

        then:
        result.models().first().stability() == "unknown"
    }

    def "should lowercase model stability when present"() {
        given:
        Architecture architecture = architecture(["text"], ["text"])
        Pricing pricing = pricing("0.01", "0.02")
        RawModel rawModel = new RawModel(
                "gpt-4.1-mini", "Model Ten", "desc",
                architecture, [], pricing, 1024, false, false, false, "StAbLe"
        )

        when:
        ModelsResponseDTO result = mapper.toDTO([rawModel])

        then:
        result.models().first().stability() == "stable"
    }

    def "should filter out model not in curated list"() {
        given:
        Architecture architecture = architecture(["text"], ["text"])
        Pricing pricing = pricing("0.01", "0.02")
        RawModel rawModel = new RawModel(
                "model-10", "Model Ten", "desc",
                architecture, [], pricing, 1024, false, false, false, "stable"
        )

        when:
        ModelsResponseDTO result = mapper.toDTO([rawModel])

        then:
        result.models().isEmpty()
    }

    def "should filter out model with null architecture"() {
        given:
        Pricing pricing = pricing("0.01", "0.02")
        RawModel rawModel = new RawModel(
                "model-10", "Model Ten", "desc",
                null, [], pricing, 1024, false, false, false, "stable"
        )

        when:
        ModelsResponseDTO result = mapper.toDTO([rawModel])

        then:
        result.models().isEmpty()
    }

    def "should filter out model that does not support text input"() {
        given:
        Architecture architecture = architecture(["image"], ["text"])
        Pricing pricing = pricing("0.01", "0.02")
        RawModel rawModel = new RawModel(
                "model-10", "Model Ten", "desc",
                architecture, [], pricing, 1024, false, false, false, "stable"
        )

        when:
        ModelsResponseDTO result = mapper.toDTO([rawModel])

        then:
        result.models().isEmpty()
    }

    def "should filter out model that does not support text output"() {
        given:
        Architecture architecture = architecture(["text"], ["image"])
        Pricing pricing = pricing("0.01", "0.02")
        RawModel rawModel = new RawModel(
                "model-10", "Model Ten", "desc",
                architecture, [], pricing, 1024, false, false, false, "stable"
        )

        when:
        ModelsResponseDTO result = mapper.toDTO([rawModel])

        then:
        result.models().isEmpty()
    }

    def "should filter out model with zero context length"() {
        given:
        Architecture architecture = architecture(["text"], ["text"])
        Pricing pricing = pricing("0.01", "0.02")
        RawModel rawModel = new RawModel(
                "model-10", "Model Ten", "desc",
                architecture, [], pricing, 0, false, false, false, "stable"
        )

        when:
        ModelsResponseDTO result = mapper.toDTO([rawModel])

        then:
        result.models().isEmpty()
    }

    def "should filter out model with negative context length"() {
        given:
        Architecture architecture = architecture(["text"], ["text"])
        Pricing pricing = pricing("0.01", "0.02")
        RawModel rawModel = new RawModel(
                "model-10", "Model Ten", "desc",
                architecture, [], pricing, -1, false, false, false, "stable"
        )

        when:
        ModelsResponseDTO result = mapper.toDTO([rawModel])

        then:
        result.models().isEmpty()
    }

    def "should filter out model marked as unstable"() {
        given:
        Architecture architecture = architecture(["text"], ["text"])
        Pricing pricing = pricing("0.01", "0.02")
        RawModel rawModel = new RawModel(
                "model-10", "Model Ten", "desc",
                architecture, [], pricing, 1024, false, false, false, "unstable"
        )

        when:
        ModelsResponseDTO result = mapper.toDTO([rawModel])

        then:
        result.models().isEmpty()
    }

    def "should return empty response when input list is empty"() {
        when:
        ModelsResponseDTO result = mapper.toDTO([])

        then:
        result.models().isEmpty()
    }

    static Architecture architecture(def input = [], def output = [], String tokenizer = "gpt") {
        return new Architecture(input, output, tokenizer)
    }

    static Pricing pricing(String prompt = "0", String completion = "1", String request = "3") {
        return new Pricing(prompt, completion, request)
    }
}