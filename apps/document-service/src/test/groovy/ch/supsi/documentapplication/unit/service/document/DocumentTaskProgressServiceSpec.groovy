package ch.supsi.documentapplication.unit.service.document

import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentTaskStorage
import ch.supsi.documentapplication.model.TaskType
import ch.supsi.documentapplication.service.document.DocumentTaskProgressService
import spock.lang.Specification

class DocumentTaskProgressServiceSpec extends Specification {

    IDatabaseDocumentTaskStorage documentTaskStorage = Mock()
    DocumentTaskProgressService service = new DocumentTaskProgressService(documentTaskStorage)

    def "should start task via upsert"() {
        when:
        service.startTask(1L, TaskType.OCR, 10)

        then:
        1 * documentTaskStorage.upsert(1L, TaskType.OCR, 10)
    }

    def "should update progress"() {
        when:
        service.updateProgress(2L, TaskType.SEGMENTATION, 3)

        then:
        1 * documentTaskStorage.updateProgress(2L, TaskType.SEGMENTATION, 3)
    }

    def "should complete task"() {
        when:
        service.completeTask(3L, TaskType.SUMMARIZATION)

        then:
        1 * documentTaskStorage.complete(3L, TaskType.SUMMARIZATION)
    }

    def "should fail task"() {
        when:
        service.failTask(4L, TaskType.TOPIC)

        then:
        1 * documentTaskStorage.fail(4L, TaskType.TOPIC)
    }
}
