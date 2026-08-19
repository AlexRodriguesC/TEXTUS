package ch.supsi.documentapplication.unit.exception

import ch.supsi.documentapplication.exception.GlobalExceptionHandler
import ch.supsi.documentapplication.exception.NotFoundException
import ch.supsi.documentapplication.exception.UnauthorizedException
import ch.supsi.documentapplication.exception.UsernameTakenException
import org.springframework.http.HttpStatus
import spock.lang.Specification
import spock.lang.Subject

class GlobalExceptionHandlerSpec extends Specification {

    @Subject
    GlobalExceptionHandler handler = new GlobalExceptionHandler()

    def "should map NotFoundException to 404"() {
        when:
        def response = handler.notFoundException(NotFoundException.spaceNotFoundException(1L))

        then:
        response.statusCode == HttpStatus.NOT_FOUND
        response.body.message().contains("1")
    }

    def "should map UnauthorizedException to 401"() {
        when:
        def response = handler.unauthorizedException(UnauthorizedException.userNotOwnerOfSpace("alice", 1L))

        then:
        response.statusCode == HttpStatus.UNAUTHORIZED
    }

    def "should map UsernameTakenException to 409"() {
        when:
        def response = handler.usernameTakenException(new UsernameTakenException("alice"))

        then:
        response.statusCode == HttpStatus.CONFLICT
    }

    def "should map generic Exception to 500 ProblemDetail"() {
        when:
        def detail = handler.handle(new RuntimeException("boom"))

        then:
        detail.status == 500
    }
}
