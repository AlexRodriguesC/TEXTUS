package ch.supsi.documentapplication.unit.service.clients

import ch.supsi.documentapplication.service.clients.LLMInputSanitizer
import ch.supsi.documentapplication.service.clients.StringSanitizer
import spock.lang.Specification
import spock.lang.Unroll

class LLMInputSanitizerSpec extends Specification {

    StringSanitizer sanitizer = new LLMInputSanitizer()

    def "returns null for null input"() {
        expect:
        sanitizer.sanitize(null) == null
    }

    def "returns plain text unchanged"() {
        expect:
        sanitizer.sanitize('{"key":"rasterization"}') == '{"key":"rasterization"}'
    }

    @Unroll
    def "strips markdown fences: #description"() {
        expect:
        sanitizer.sanitize(input) == expected

        where:
        description                     | input                                      | expected
        "```json fence"                 | "```json\n{\"key\":\"rasterization\"}\n```" | '{"key":"rasterization"}'
        "plain ``` fence"               | "```\n{\"key\":\"rasterization\"}\n```"     | '{"key":"rasterization"}'
        "surrounded by whitespace"       | "  ```json\n{\"key\":\"v\"}\n```  "         | '{"key":"v"}'
        "no newline after opening fence" | "```json{\"key\":\"rasterization\"}```"     | '{"key":"rasterization"}'
    }

    def "removes only the outermost fences and keeps inner content"() {
        expect:
        sanitizer.sanitize("```json\n{\"text\":\"```not a fence```\"}\n```") ==
                '{"text":"```not a fence```"}'
    }

    def "keeps inner whitespace trimming behavior"() {
        expect:
        sanitizer.sanitize("   ```json\n{\"key\":\"rasterization\"}\n```   ") ==
                '{"key":"rasterization"}'
    }

    def "returns blank string for blank input"() {
        expect:
        sanitizer.sanitize("   ") == ""
    }
}
