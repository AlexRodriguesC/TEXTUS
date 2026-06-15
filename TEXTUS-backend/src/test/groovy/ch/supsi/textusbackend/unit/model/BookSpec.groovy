package ch.supsi.textusbackend.unit.model


import ch.supsi.textusbackend.model.Book
import spock.lang.Specification

class BookSpec extends Specification {

    def "should create book"() {
        given:
        UUID randomUUID = UUID.randomUUID()
        Long bookId = new Random().nextLong()
        var randomFilename = randomUUID.toString()
        var size = new Random().nextLong(0, Long.MAX_VALUE)
        var bytes = randomUUID.toString().getBytes()

        when:
        Book created = Book.create(bookId, randomFilename, size, bytes)

        then:
        created.bookId == bookId
        created.originalFilename == randomFilename
        created.size == size
        created.bytes == bytes
    }

    def "should create book, no ext UUID version"() {
        given:
        UUID randomUUID = UUID.randomUUID()
        var randomFilename = randomUUID.toString()
        var size = new Random().nextLong(0, Long.MAX_VALUE)
        var bytes = randomUUID.toString().getBytes()

        when:
        Book created = Book.create(randomFilename, size, bytes)

        then:
        created.bookId != null
        created.originalFilename == randomFilename
        created.size == size
        created.bytes == bytes
    }

    def "should throw if size is less than or equal than 0"() {
        given:
        UUID randomUUID = UUID.randomUUID()
        Long bookId = new Random().nextLong()
        var randomFilename = randomUUID.toString()
        var size = -1
        var bytes = randomUUID.toString().getBytes()

        when:
        Book created = Book.create(bookId, randomFilename, size, bytes)

        then:
        created == null
        def e = thrown(IllegalArgumentException)
        e.message == "Book cannot be empty"
    }


}
