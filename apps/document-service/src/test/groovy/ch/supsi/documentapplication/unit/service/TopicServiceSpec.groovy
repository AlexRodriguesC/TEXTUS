package ch.supsi.documentapplication.unit.service

import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage
import ch.supsi.documentapplication.exception.NotFoundException
import ch.supsi.documentapplication.exception.UnauthorizedException
import ch.supsi.documentapplication.model.Space
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.service.TopicService
import spock.lang.Specification

class TopicServiceSpec extends Specification {

    def topicStorage = Mock(IDatabaseTopicStorage)
    def spaceStorage = Mock(IDatabaseSpaceStorage)
    def service = new TopicService(topicStorage, spaceStorage)

    def "should return topics for space owner"() {
        given:
        def topics = [new Topic(1L, "a.pdf", 1, "title", "summary", [])]

        when:
        def result = service.getTopicsBySpace("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> Optional.of(new Space(10L, "alice", "", "", ""))
        1 * topicStorage.getBySpaceId(10L) >> topics
        result == topics
    }

    def "should throw not found when space does not exist"() {
        when:
        service.getTopicsBySpace("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> Optional.empty()
        thrown(NotFoundException)
        0 * topicStorage._
    }

    def "should throw unauthorized when requester is not owner"() {
        when:
        service.getTopicsBySpace("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> Optional.of(new Space(10L, "bob", "", "", ""))
        thrown(UnauthorizedException)
        0 * topicStorage._
    }
}
