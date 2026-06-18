package ch.supsi.textusbackend.unit.model

import ch.supsi.textusbackend.model.Chunk
import ch.supsi.textusbackend.model.ChunkDeserializer
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
            "page": 3,
            "title": "Rasterization",
            "summary": "Conversion of 3D primitives to 2D image",
            "key": "rasterization",
            "keywords": ["opengl", "pipeline"],
            "questions": ["What is rasterization?"],
            "related_keys": ["shading", "texturing"],
            "entities": [
                {"name": "OpenGL", "type": "TECHNOLOGY"}
            ]
        }
        """

        when:
        Chunk chunk = mapper.readValue(json, Chunk.class)

        then:
        chunk.page == 3
        chunk.title == "Rasterization"
        chunk.summary == "Conversion of 3D primitives to 2D image"
        chunk.key == "rasterization"
        chunk.keywords == ["opengl", "pipeline"]
        chunk.questions == ["What is rasterization?"]
        chunk.relatedKeys == ["shading", "texturing"]
        chunk.entities.size() == 1
        chunk.entities[0].name() == "OpenGL"
        chunk.entities[0].type() == "TECHNOLOGY"
    }

    def "deserializes chunk with empty lists"() {
        given:
        def json = """
        {
            "page": 1,
            "title": "Phong Shading",
            "summary": "Per-pixel lighting model",
            "key": "phong-shading",
            "keywords": [],
            "questions": [],
            "related_keys": [],
            "entities": []
        }
        """

        when:
        Chunk chunk = mapper.readValue(json, Chunk.class)

        then:
        chunk.page == 1
        chunk.title == "Phong Shading"
        chunk.keywords == []
        chunk.questions == []
        chunk.relatedKeys == []
        chunk.entities == []
    }

    def "skips null items inside string lists"() {
        given:
        def json = """
        {
            "page": 1,
            "title": "Mipmapping",
            "summary": "Level of detail technique",
            "key": "mipmapping",
            "keywords": ["lod", null, "vram"],
            "questions": [],
            "related_keys": [],
            "entities": []
        }
        """

        when:
        Chunk chunk = mapper.readValue(json, Chunk.class)

        then:
        chunk.keywords == ["lod", "vram"]
    }

    def "skips null items inside entities list"() {
        given:
        def json = """
        {
            "page": 1,
            "title": "Texture Mapping",
            "summary": "Applying 2D images to 3D surfaces",
            "key": "texture-mapping",
            "keywords": [],
            "questions": [],
            "related_keys": [],
            "entities": [{"name": "GLSL", "type": "TECHNOLOGY"}, null]
        }
        """

        when:
        Chunk chunk = mapper.readValue(json, Chunk.class)

        then:
        chunk.entities.size() == 1
        chunk.entities[0].name() == "GLSL"
    }

    def "tokenCountInput and tokenCountOutput are not deserialized"() {
        given:
        def json = """
        {
            "page": 1,
            "title": "Z-Buffer",
            "summary": "Depth testing mechanism",
            "key": "z-buffer",
            "keywords": [],
            "questions": [],
            "related_keys": [],
            "entities": [],
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
            "page": 1,
            "summary": "Per-pixel lighting model",
            "key": "phong-shading",
            "keywords": [], "questions": [], "related_keys": [], "entities": []
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
            "page": 1,
            "title": null,
            "summary": "Per-pixel lighting model",
            "key": "phong-shading",
            "keywords": [], "questions": [], "related_keys": [], "entities": []
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
            "page": 1,
            "title": 123,
            "summary": "Per-pixel lighting model",
            "key": "phong-shading",
            "keywords": [], "questions": [], "related_keys": [], "entities": []
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
            "page": 1,
            "title": "Phong Shading",
            "key": "phong-shading",
            "keywords": [], "questions": [], "related_keys": [], "entities": []
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
            "page": 1,
            "title": "Phong Shading",
            "summary": "Per-pixel lighting model",
            "keywords": [], "questions": [], "related_keys": [], "entities": []
        }
        """

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains("key")
    }

    def "throws when page is missing"() {
        given:
        def json = """
        {
            "title": "Z-Buffer",
            "summary": "Depth testing mechanism",
            "key": "z-buffer",
            "keywords": [], "questions": [], "related_keys": [], "entities": []
        }
        """

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains("page")
    }

    def "throws when page is null"() {
        given:
        def json = """
        {
            "page": null,
            "title": "Z-Buffer",
            "summary": "Depth testing mechanism",
            "key": "z-buffer",
            "keywords": [], "questions": [], "related_keys": [], "entities": []
        }
        """

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains("page")
    }

    def "throws when page is not a number"() {
        given:
        def json = """
        {
            "page": "three",
            "title": "Z-Buffer",
            "summary": "Depth testing mechanism",
            "key": "z-buffer",
            "keywords": [], "questions": [], "related_keys": [], "entities": []
        }
        """

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains("page")
    }

    def "throws when string list field is missing"(String field) {
        given:
        def base = [page: 1, title: "Mipmapping", summary: "LOD technique", key: "mipmapping",
                    keywords: [], questions: [], "related_keys": [], entities: []]
        base.remove(field)
        def json = new ObjectMapper().writeValueAsString(base)

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains(field)

        where:
        field << ["keywords", "questions", "related_keys"]
    }

    def "throws when string list field is null"(String field) {
        given:
        def base = [page: 1, title: "Mipmapping", summary: "LOD technique", key: "mipmapping",
                    keywords: [], questions: [], "related_keys": [], entities: []]
        base[field] = null
        def json = new ObjectMapper().writeValueAsString(base)

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains(field)

        where:
        field << ["keywords", "questions", "related_keys"]
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
        field << ["keywords", "questions", "related_keys"]
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
        field << ["keywords", "questions", "related_keys"]
    }

    def "throws when entities field is missing"() {
        given:
        def json = """
        {
            "page": 1, "title": "COLLADA", "summary": "3D interchange format", "key": "collada",
            "keywords": [], "questions": [], "related_keys": []
        }
        """

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains("entities")
    }

    def "throws when entities field is null"() {
        given:
        def json = validJson([entities: null])

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains("entities")
    }

    def "throws when entities field is not an array"() {
        given:
        def json = validJson([entities: [name: "GLSL", type: "TECHNOLOGY"]])

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains("entities")
    }

    def "throws when an entity item is not an object"() {
        given:
        def json = validJson([entities: ["not-an-object"]])

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains("entities[0]")
    }

    def "throws when entity name is missing"() {
        given:
        def json = validJson([entities: [[type: "TECHNOLOGY"]]])

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains("name")
    }

    def "throws when entity name is null"() {
        given:
        def json = validJson([entities: [[name: null, type: "TECHNOLOGY"]]])

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains("name")
    }

    def "throws when entity name is not a string"() {
        given:
        def json = validJson([entities: [[name: 99, type: "TECHNOLOGY"]]])

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains("name")
    }

    def "throws when entity type is missing"() {
        given:
        def json = validJson([entities: [[name: "GLSL"]]])

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains("type")
    }

    def "throws when entity type is null"() {
        given:
        def json = validJson([entities: [[name: "GLSL", type: null]]])

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains("type")
    }

    def "throws when entity type is not a string"() {
        given:
        def json = validJson([entities: [[name: "GLSL", type: true]]])

        when:
        mapper.readValue(json, Chunk.class)

        then:
        def ex = thrown(JsonParseException)
        ex.message.contains("type")
    }

    private static String validJson(Map overrides = [:]) {
        def base = [
                page        : 1,
                title       : "GLSL Shaders",
                summary     : "Programmable shading pipeline",
                key         : "glsl-shaders",
                keywords    : [],
                questions   : [],
                "related_keys": [],
                entities    : []
        ]
        base.putAll(overrides)
        new ObjectMapper().writeValueAsString(base)
    }
}