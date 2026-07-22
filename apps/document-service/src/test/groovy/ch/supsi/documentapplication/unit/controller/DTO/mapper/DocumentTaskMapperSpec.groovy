package ch.supsi.documentapplication.unit.controller.DTO.mapper

import ch.supsi.documentapplication.controller.DTO.mapper.DocumentTaskMapper
import ch.supsi.documentapplication.model.DocumentTask
import ch.supsi.documentapplication.model.TaskType
import ch.supsi.documentapplication.model.TaskVariant
import spock.lang.Specification

class DocumentTaskMapperSpec extends Specification {

    DocumentTaskMapper mapper = new DocumentTaskMapper()

    def "should map document task to dto"() {
        given:
        def task = new DocumentTask("5:OCR", 5L, TaskType.OCR, 3, 10, TaskVariant.DEFAULT)

        when:
        def dto = mapper.toDto(task)

        then:
        dto.id() == "5:OCR"
        dto.type() == TaskType.OCR
        dto.progress() == 3
        dto.maxProgress() == 10
        dto.variant() == TaskVariant.DEFAULT
    }

    def "should map list of document tasks"() {
        given:
        def tasks = [
                new DocumentTask("1:OCR", 1L, TaskType.OCR, 1, 1, TaskVariant.SUCCESS),
                new DocumentTask("1:TOPIC", 1L, TaskType.TOPIC, 0, 2, TaskVariant.DEFAULT),
        ]

        when:
        def dtos = mapper.toDtoList(tasks)

        then:
        dtos.size() == 2
        dtos[0].type() == TaskType.OCR
        dtos[1].type() == TaskType.TOPIC
    }
}
