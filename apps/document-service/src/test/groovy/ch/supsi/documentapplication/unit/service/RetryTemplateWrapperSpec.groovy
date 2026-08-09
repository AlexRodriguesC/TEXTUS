package ch.supsi.documentapplication.unit.service

import ch.supsi.documentapplication.exception.ModelFallbackExhaustedException
import ch.supsi.documentapplication.exception.RetriesFailedException
import ch.supsi.documentapplication.service.RetryTemplateWrapper
import org.springframework.ai.retry.TransientAiException
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.retry.RetryTemplate
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.http.HttpHeaders
import spock.lang.Specification

import java.time.Duration

class RetryTemplateWrapperSpec extends Specification {

    RetryTemplateWrapper wrapper

    def setup() {
        def retryPolicy = RetryPolicy.builder()
                .maxRetries(2)
                .includes(TransientAiException.class)
                .delay(Duration.ofMillis(10))
                .build()
        wrapper = new RetryTemplateWrapper(new RetryTemplate(retryPolicy))
    }

    def "should return result when retryable succeeds"() {
        when:
        def result = wrapper.execute(() -> "ok")

        then:
        result == "ok"
    }

    def "should map exhausted retries to RetriesFailedException preserving last cause"() {
        given:
        def lastCause = WebClientResponseException.create(429, "Too Many Requests", HttpHeaders.EMPTY, null, null)

        when:
        wrapper.execute(() -> {
            throw ModelFallbackExhaustedException.create(3, "last-model", lastCause)
        })

        then:
        def thrown = thrown(RetriesFailedException)
        thrown.cause == lastCause || thrown.cause?.cause == lastCause
        thrown.message.contains("Exhausted retries")
    }

    def "should fail fast without wrapping non-retryable exceptions"() {
        when:
        wrapper.execute(() -> {
            throw new IllegalStateException("misconfiguration")
        })

        then:
        thrown(IllegalStateException)
    }
}
