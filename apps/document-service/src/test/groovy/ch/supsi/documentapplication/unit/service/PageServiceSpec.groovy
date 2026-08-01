package ch.supsi.documentapplication.unit.service

import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage
import ch.supsi.documentapplication.model.Space
import ch.supsi.documentapplication.model.SpacePage
import ch.supsi.documentapplication.service.PageService
import spock.lang.Specification

class PageServiceSpec extends Specification {

    IDatabaseDocumentStorage databaseDocumentStorage = Mock()
    IDatabaseSpaceStorage databaseSpaceStorage = Mock()
    PageService service = new PageService(databaseDocumentStorage, databaseSpaceStorage)

    def "should return pages when space belongs to owner"() {
        given:
        databaseSpaceStorage.read(10L) >> Optional.of(new Space(10L, "alice", "icon", "name", "desc"))
        databaseDocumentStorage.getPagesBySpaceId(10L) >> [new SpacePage(1L, 5L, 1, "Title", "Summary")]

        when:
        def result = service.getPagesBySpace("alice", 10L)

        then:
        result.size() == 1
        result[0].id == 1L
    }
}
