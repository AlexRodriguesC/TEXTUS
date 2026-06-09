package ch.supsi.textusbackend.unit.model


import ch.supsi.textusbackend.model.Document
import spock.lang.Specification

class DocumentSpec extends Specification {

    def "should create document with UUID"() {
        given:
        def bytes = "abc".getBytes()

        when:
        Document document = Document.create(bytes)

        then:
        bytes == document.getBytes()
        document.getDocumentId() != null
    }

    def "should create document without UUID"() {
        given:
        UUID uuid = UUID.randomUUID()
        def bytes = "abc".getBytes()

        when:
        Document document = Document.create(uuid, bytes)

        then:
        uuid == document.getDocumentId()
        bytes == document.getBytes()
    }


}
