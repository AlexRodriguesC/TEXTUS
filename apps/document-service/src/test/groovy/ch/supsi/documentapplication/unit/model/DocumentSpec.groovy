package ch.supsi.documentapplication.unit.model


import ch.supsi.documentapplication.model.FsDocument
import spock.lang.Specification

class DocumentSpec extends Specification {

    def "should create document with UUID"() {
        given:
        def bytes = "abc".getBytes()

        when:
        FsDocument document = FsDocument.create(bytes)

        then:
        bytes == document.getBytes()
        document.getDocumentId() != null
    }

    def "should create document without UUID"() {
        given:
        UUID uuid = UUID.randomUUID()
        def bytes = "abc".getBytes()

        when:
        FsDocument document = FsDocument.create(uuid, bytes)

        then:
        uuid == document.getDocumentId()
        bytes == document.getBytes()
    }


}
