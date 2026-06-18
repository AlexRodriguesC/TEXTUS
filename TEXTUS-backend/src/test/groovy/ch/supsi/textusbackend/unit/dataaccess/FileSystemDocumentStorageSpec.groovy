package ch.supsi.textusbackend.unit.dataaccess

import ch.supsi.textusbackend.dataaccess.FileSystemDocumentStorage
import ch.supsi.textusbackend.exception.DocumentDomainException
import ch.supsi.textusbackend.model.Document
import ch.supsi.textusbackend.properties.AppProperties
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

class FileSystemDocumentStorageSpec extends Specification {

    @TempDir
    Path tempDir

    FileSystemDocumentStorage storage
    Path documentDir

    def setup() {
        documentDir = tempDir.resolve("documents")

        def appProperties = Mock(AppProperties) {
            getBaseFilesystemPath() >> documentDir.toString()
        }

        storage = new FileSystemDocumentStorage(appProperties)
    }

    private Document mockDocument(byte[] bytes = "content".bytes) {
        Mock(Document) {
            getDocumentId() >> UUID.randomUUID()
            getBytes() >> bytes
        }
    }

    private Document mockDocument(UUID uuid, byte[] bytes) {
        Mock(Document) {
            getDocumentId() >> uuid
            getBytes() >> bytes
        }
    }

    def "should create document"() {
        given:
        Files.createDirectory(documentDir)
        def document = mockDocument()

        when:
        storage.create(document)

        then:
        Files.exists(documentDir.resolve(document.getDocumentId().toString()))
        Files.readAllBytes(documentDir.resolve(document.getDocumentId().toString())) == "content".bytes
    }

    def "should create directory if it doesn't exist"() {
        given:
        def document = mockDocument()

        when:
        storage.create(document)

        then:
        Files.exists(documentDir)
        Files.exists(documentDir.resolve(document.getDocumentId().toString()))
    }

    def "should throw exception if it fails to create base directory"() {
        given:
        def appProperties = Mock(AppProperties) {
            getBaseFilesystemPath() >> "/no/such/parent/documents"
        }
        def failingStorage = new FileSystemDocumentStorage(appProperties)

        when:
        failingStorage.create(mockDocument())

        then:
        thrown(DocumentDomainException)
    }

    def "should throw if it fails to create document file"() {
        given:
        Files.createDirectory(documentDir)
        def document = mockDocument()
        Files.createDirectory(documentDir.resolve(document.getDocumentId().toString()))

        when:
        storage.create(document)

        then:
        thrown(DocumentDomainException)
    }

    def "should read document"() {
        given:
        Files.createDirectory(documentDir)
        byte[] content = "hello document".bytes
        Document document = mockDocument(content)
        Files.write(documentDir.resolve(document.getDocumentId().toString()), content)

        when:
        def result = storage.read(document.getDocumentId())

        then:
        result != null
        result.getBytes() == content
    }

    def "should return null if base path does not exist"() {
        given:
        def documentId = UUID.randomUUID()

        when:
        def result = storage.read(documentId)

        then:
        result == null
    }

    def "should return null if document file does not exist"() {
        given:
        Files.createDirectory(documentDir)
        def documentId = UUID.randomUUID()

        when:
        def result = storage.read(documentId)

        then:
        result == null
    }

    def "should throw if unable to read document"() {
        given:
        Files.createDirectory(documentDir)
        // directory at file path causes FileInputStream to throw
        def documentId = UUID.randomUUID()
        Files.createDirectory(documentDir.resolve(documentId.toString()))

        when:
        storage.read(documentId)

        then:
        thrown(DocumentDomainException)
    }

    def "should update document"() {
        given:
        Files.createDirectory(documentDir)
        def document = mockDocument("old content".getBytes())
        Files.write(documentDir.resolve(document.getDocumentId().toString()), document.getBytes())
        document = mockDocument(document.getDocumentId(), "new content".bytes)

        when:
        storage.update(document)

        then:
        Files.readAllBytes(documentDir.resolve(document.getDocumentId().toString())) == "new content".bytes
    }

    def "should delete document"() {
        given:
        Files.createDirectory(documentDir)
        def document = mockDocument("data".bytes)
        Files.write(documentDir.resolve(document.getDocumentId().toString()), document.bytes)

        when:
        storage.delete(document.getDocumentId())

        then:
        !Files.exists(documentDir.resolve("123"))
    }

    def "should warn about trying to delete a non-existent document"() {
        given:
        def documentId = UUID.randomUUID()

        when:
        storage.delete(documentId)

        then:
        noExceptionThrown()
    }

    def "should warn about trying to delete a non-existent document file"() {
        given:
        Files.createDirectory(documentDir)
        def documentId = UUID.randomUUID()

        when:
        storage.delete(documentId)

        then:
        noExceptionThrown()
    }

    def "should throw if unable to delete document"() {
        given:
        Files.createDirectory(documentDir)
        def documentId = UUID.randomUUID()
        def documentPath = documentDir.resolve(documentId.toString())
        Files.createDirectory(documentPath)
        Files.write(documentPath.resolve("blocker"), "x".bytes)

        when:
        storage.delete(documentId)

        then:
        thrown(DocumentDomainException)
    }
}
