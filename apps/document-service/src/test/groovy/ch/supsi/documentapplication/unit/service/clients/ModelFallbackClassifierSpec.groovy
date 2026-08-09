package ch.supsi.documentapplication.unit.service.clients

import ch.supsi.documentapplication.exception.AIOutputMappingFailedException
import ch.supsi.documentapplication.service.clients.ModelFallbackClassifier
import org.springframework.ai.retry.NonTransientAiException
import org.springframework.ai.retry.TransientAiException
import org.springframework.http.HttpHeaders
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.reactive.function.client.WebClientResponseException
import spock.lang.Specification
import spock.lang.Unroll

class ModelFallbackClassifierSpec extends Specification {

    @Unroll
    def "should treat #description as fallback-eligible"() {
        expect:
        ModelFallbackClassifier.isFallbackEligible(exception)

        where:
        description                         | exception
        "HTTP 429"                          | httpStatus(429)
        "HTTP 502"                          | httpStatus(502)
        "HTTP 503"                          | httpStatus(503)
        "TransientAiException"              | new TransientAiException("temporary")
        "AIOutputMappingFailedException"    | AIOutputMappingFailedException.create("x", "y")
        "ResourceAccessException"           | new ResourceAccessException("connection reset")
        "NonTransient rate-limit message"   | new NonTransientAiException("429 rate limit exceeded")
        "wrapped eligible cause"            | new RuntimeException("wrapper", httpStatus(503))
    }

    @Unroll
    def "should treat #description as not fallback-eligible"() {
        expect:
        !ModelFallbackClassifier.isFallbackEligible(exception)

        where:
        description              | exception
        "HTTP 400"               | httpStatus(400)
        "IllegalStateException"  | new IllegalStateException("bad config")
        "NullPointerException"   | new NullPointerException("bug")
        "generic RuntimeException" | new RuntimeException("boom")
        "NonTransient unrelated" | new NonTransientAiException("invalid request schema")
    }

    private static WebClientResponseException httpStatus(int status) {
        return WebClientResponseException.create(status, "status", HttpHeaders.EMPTY, null, null)
    }
}
