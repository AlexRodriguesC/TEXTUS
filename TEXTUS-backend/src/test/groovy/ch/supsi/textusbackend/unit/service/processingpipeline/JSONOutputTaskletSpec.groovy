package ch.supsi.textusbackend.unit.service.processingpipeline

import ch.supsi.textusbackend.dataaccess.IDatabaseBookStorage
import ch.supsi.textusbackend.dataaccess.IFileSystemBookStorage
import ch.supsi.textusbackend.model.Chunk
import ch.supsi.textusbackend.service.processingpipeline.JSONOutputTasklet
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

class JSONOutputTaskletSpec extends Specification {

    IDatabaseBookStorage databaseBookStorage = Mock()
    IFileSystemBookStorage fileSystemBookStorage = Mock()
    StepContribution stepContribution = Mock()
    ChunkContext chunkContext = Mock()

    JSONOutputTasklet jsonOutputTasklet = new JSONOutputTasklet(databaseBookStorage, fileSystemBookStorage)

    def "should create json output"() {
        given:
        Long expectedBookId = 42L
        List<Chunk> expectedChunks = List.of()
        ReflectionTestUtils.setField(jsonOutputTasklet, "bookId", expectedBookId)
        databaseBookStorage.readChunks(_ as Long) >> expectedChunks
        def expectedStatus = RepeatStatus.FINISHED

        when:
        def actualStatus = jsonOutputTasklet.execute(stepContribution, chunkContext)

        then:
        1 * databaseBookStorage.readChunks(expectedBookId)
        1 * fileSystemBookStorage.createChunkJSON(expectedChunks, expectedBookId)
        expectedStatus == actualStatus
    }

}
