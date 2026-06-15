package ch.supsi.textusbackend.unit.dataaccess

import ch.supsi.textusbackend.dataaccess.FileSystemBookStorage
import ch.supsi.textusbackend.exception.BookDomainException
import ch.supsi.textusbackend.model.Book
import ch.supsi.textusbackend.properties.AppProperties
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

class FileSystemBookStorageSpec extends Specification {

    @TempDir
    Path tempDir

    FileSystemBookStorage storage
    Path booksDir

    def setup() {
        booksDir = tempDir.resolve("books")

        def appProperties = Mock(AppProperties) {
            getBaseFilesystemPath() >> booksDir.toString()
        }

        storage = new FileSystemBookStorage(appProperties)
    }

    private Book mockBook(byte[] bytes = "content".bytes) {
        Mock(Book) {
            getBookId() >> 123
            getBytes() >> bytes
        }
    }

    def "should create book"() {
        given:
        Files.createDirectory(booksDir)
        def book = mockBook()

        when:
        storage.create(book)

        then:
        Files.exists(booksDir.resolve("123"))
        Files.readAllBytes(booksDir.resolve("123")) == "content".bytes
    }

    def "should create directory if it doesn't exist"() {
        given:
        def book = mockBook()

        when:
        storage.create(book)

        then:
        Files.exists(booksDir)
        Files.exists(booksDir.resolve("123"))
    }

    def "should throw exception if it fails to create base directory"() {
        given:
        def appProperties = Mock(AppProperties) {
            getBaseFilesystemPath() >> "/no/such/parent/books"
        }
        def failingStorage = new FileSystemBookStorage(appProperties)

        when:
        failingStorage.create(mockBook())

        then:
        thrown(BookDomainException)
    }

    def "should throw if it fails to create book file"() {
        given:
        Files.createDirectory(booksDir)
        // place a directory where the file should be written — Files.write will fail
        Files.createDirectory(booksDir.resolve("123"))

        when:
        storage.create(mockBook())

        then:
        thrown(BookDomainException)
    }

    def "should read book"() {
        given:
        Files.createDirectory(booksDir)
        byte[] content = "hello book".bytes
        Files.write(booksDir.resolve("123"), content)
        def bookId = 123

        when:
        def result = storage.read(bookId)

        then:
        result != null
        result.getBytes() == content
    }

    def "should return null if base path does not exist"() {
        given:
        def bookId = 123

        when:
        def result = storage.read(bookId)

        then:
        result == null
    }

    def "should return null if book file does not exist"() {
        given:
        Files.createDirectory(booksDir)
        def bookId = 123

        when:
        def result = storage.read(bookId)

        then:
        result == null
    }

    def "should throw if unable to read book"() {
        given:
        Files.createDirectory(booksDir)
        // directory at file path causes FileInputStream to throw
        Files.createDirectory(booksDir.resolve("123"))
        def bookId = 123

        when:
        storage.read(bookId)

        then:
        thrown(BookDomainException)
    }

    def "should update book"() {
        given:
        Files.createDirectory(booksDir)
        Files.write(booksDir.resolve("123"), "old content".bytes)

        when:
        storage.update(mockBook("new content".bytes))

        then:
        Files.readAllBytes(booksDir.resolve("123")) == "new content".bytes
    }

    def "should delete book"() {
        given:
        Files.createDirectory(booksDir)
        Files.write(booksDir.resolve("123"), "data".bytes)
        def bookId = 123

        when:
        storage.delete(bookId)

        then:
        !Files.exists(booksDir.resolve("123"))
    }

    def "should warn about trying to delete a non-existent book"() {
        given:
        def bookId = 123

        when:
        storage.delete(bookId)

        then:
        noExceptionThrown()
    }

    def "should warn about trying to delete a non-existent book file"() {
        given:
        Files.createDirectory(booksDir)
        def bookId = 123

        when:
        storage.delete(bookId)

        then:
        noExceptionThrown()
    }

    def "should throw if unable to delete book"() {
        given:
        Files.createDirectory(booksDir)
        def bookPath = booksDir.resolve("123")
        Files.createDirectory(bookPath)
        Files.write(bookPath.resolve("blocker"), "x".bytes)
        def bookId = 123

        when:
        storage.delete(bookId)

        then:
        thrown(BookDomainException)
    }
}
