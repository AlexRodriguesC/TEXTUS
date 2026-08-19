package ch.supsi.documentapplication.unit.service.rag

import ch.supsi.documentapplication.service.rag.ChatRagFilterExpressions
import org.springframework.ai.vectorstore.filter.Filter
import spock.lang.Specification

class ChatRagFilterExpressionsSpec extends Specification {

    def "spaceIdEquals builds eq filter for the given space"() {
        when:
        Filter.Expression expression = ChatRagFilterExpressions.spaceIdEquals(42L)

        then:
        expression != null
        expression.toString().contains("spaceId")
        expression.toString().contains("42")
    }

    def "spaceIdEquals differs for different spaces"() {
        expect:
        ChatRagFilterExpressions.spaceIdEquals(1L).toString() !=
                ChatRagFilterExpressions.spaceIdEquals(2L).toString()
    }
}
