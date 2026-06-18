package ch.supsi.textusbackend.unit.service.clients

import ch.supsi.textusbackend.exception.BookDomainException
import ch.supsi.textusbackend.service.clients.FailureTracker
import ch.supsi.textusbackend.service.clients.LLMFailureTracker
import spock.lang.Specification

class LLMFailureTrackerSpec extends Specification {

    def "should not throw if current failures < max failures"() {
        given:
        FailureTracker failureTracker = LLMFailureTracker.create(5)

        when:
        failureTracker.registerFailure()

        then:
        noExceptionThrown()
    }

    def "should reset failures"() {
        given:
        FailureTracker failureTracker = LLMFailureTracker.create(2)

        when: '1 failure is registered'
        1.times {i -> failureTracker.registerFailure()}

        then:
        noExceptionThrown()

        when: 'failures are reset, registering another failure should not trigger the exception'
        failureTracker.resetFailures()
        failureTracker.registerFailure()

        then:
        noExceptionThrown()
    }

    def "should throw if current failures > max failures"() {
        given:
        FailureTracker failureTracker = LLMFailureTracker.create(2)

        when:
        2.times { i -> failureTracker.registerFailure() }

        then:
        thrown(BookDomainException)
    }

    def "should throw if invalid max failures is provided. Case: max failure is #str"() {
        when:
        FailureTracker failureTracker = LLMFailureTracker.create(value)

        then:
        failureTracker == null
        thrown(IllegalArgumentException)

        where:
        str        | value
        'null'     | null
        'negative' | -1
    }
}
