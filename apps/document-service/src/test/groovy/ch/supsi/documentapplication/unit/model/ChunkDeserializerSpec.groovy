package ch.supsi.documentapplication.unit.model

import ch.supsi.documentapplication.model.Chunk
import ch.supsi.documentapplication.model.ChunkDeserializer
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import spock.lang.Specification
import spock.lang.Subject

class ChunkDeserializerSpec extends Specification {

    @Subject
    ObjectMapper mapper

    def setup() {
        def module = new SimpleModule()
        module.addDeserializer(Chunk, new ChunkDeserializer())
        mapper = new ObjectMapper().registerModule(module)
    }

    def "deserializes a fully populated valid chunk"() {
        given:
        def json = """
        {
            "title": "Rasterization",
            "summary": "Conversion of 3D primitives to 2D image",
            "key": "rasterization",
            "keywords": ["opengl", "pipeline"],
            "related_keys": ["shading", "texturing"]
        }
        """

        when:
        Chunk chunk = mapper.readValue(json, Chunk.class)

        then:
        chunk.title == "Rasterization"
        chunk.summary == "Conversion of 3D primitives to 2D image"
        chunk.key == "rasterization"
        chunk.keywords == ["opengl", "pipeline"]
        chunk.relatedKeys == ["shading", "texturing"]
    }

    def "deserializes chunk with empty lists"() {
        given:
        def json = """
        {
            "title": "Phong Shading",
            "summary": "Per-pixel lighting model",
            "key": "phong-shading",
            "keywords": [],
            "related_keys": []
        }
        """

        when:
        Chunk chunk = mapper.readValue(json, Chunk.class)

        then:
        chunk.title == "Phong Shading"
        chunk.keywords == []
        chunk.relatedKeys == []
    }

    def "skips null items inside string lists"() {
        given:
        def json = """
        {
            "title": "Mipmapping",
            "summary": "Level of detail technique",
            "key": "mipmapping",
            "keywords": ["lod", null, "vram"],
            "related_keys": []
        }
        """

        when:
        Chunk chunk = mapper.readValue(json, Chunk.class)

        then:
        chunk.keywords == ["lod", "vram"]
    }

    def "tokenCountInput and tokenCountOutput are not deserialized"() {
        given:
        def json = """
        {
            "title": "Z-Buffer",
            "summary": "Depth testing mechanism",
            "key": "z-buffer",
            "keywords": [],
            "related_keys": [],
            "tokenCountInput": 100,
            "tokenCountOutput": 50
        }
        """

        when:
        Chunk chunk = mapper.readValue(json, Chunk.class)

        then:
        chunk.tokenCountInput == null
        chunk.tokenCountOutput == null
    }

    def "throws when title is missing"() {
        given:
        def json = """
        {
            "summary": "Per-pixel lighting model",
            "key": "phong-shading",
            "keywords": [], "related_keys": []
        }
        """

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains("title")
    }

    def "throws when title is null"() {
        given:
        def json = """
        {
            "title": null,
            "summary": "Per-pixel lighting model",
            "key": "phong-shading",
            "keywords": [], "related_keys": []
        }
        """

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains("title")
    }

    def "throws when title is not a string"() {
        given:
        def json = """
        {
            "title": 123,
            "summary": "Per-pixel lighting model",
            "key": "phong-shading",
            "keywords": [], "related_keys": []
        }
        """

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains("title")
    }

    def "throws when summary is missing"() {
        given:
        def json = """
        {
            "title": "Phong Shading",
            "key": "phong-shading",
            "keywords": [], "related_keys": []
        }
        """

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains("summary")
    }

    def "throws when key is missing"() {
        given:
        def json = """
        {
            "title": "Phong Shading",
            "summary": "Per-pixel lighting model",
            "keywords": [], "related_keys": []
        }
        """

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains("key")
    }

    def "throws when string list field is missing"(String field) {
        given:
        def base = [title: "Mipmapping", summary: "LOD technique", key: "mipmapping",
                    keywords: [], "related_keys": []]
        base.remove(field)
        def json = new ObjectMapper().writeValueAsString(base)

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains(field)

        where:
        field << ["keywords", "related_keys"]
    }

    def "throws when string list field is null"(String field) {
        given:
        def base = [page: 1, title: "Mipmapping", summary: "LOD technique", key: "mipmapping",
                    keywords: [], "related_keys": []]
        base[field] = null
        def json = new ObjectMapper().writeValueAsString(base)

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains(field)

        where:
        field << ["keywords", "related_keys"]
    }

    def "throws when string list field is not an array"(String field) {
        given:
        def json = validJson([(field): "not-an-array"])

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains(field)

        where:
        field << ["keywords", "related_keys"]
    }

    def "throws when a string list item is not a string"(String field) {
        given:
        def json = validJson([(field): [42]])

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains(field + "[0]")

        where:
        field << ["keywords", "related_keys"]
    }

    private static String validJson(Map overrides = [:]) {
        def base = [
                title       : "GLSL Shaders",
                summary     : "Programmable shading pipeline",
                key         : "glsl-shaders",
                keywords    : [],
                "related_keys": [],
        ]
        base.putAll(overrides)
        new ObjectMapper().writeValueAsString(base)
    }
}