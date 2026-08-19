package ch.supsi.documentapplication.controller.DTO.mapper;

import ch.supsi.documentapplication.controller.DTO.response.TaskDto;
import ch.supsi.documentapplication.model.DocumentTask;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DocumentTaskMapper {

    public TaskDto toDto(DocumentTask task) {
        return new TaskDto(
                task.getId(),
                task.getType(),
                task.getProgress(),
                task.getMaxProgress(),
                task.getVariant());
    }

    public List<TaskDto> toDtoList(List<DocumentTask> tasks) {
        return tasks.stream().map(this::toDto).toList();
    }
}
