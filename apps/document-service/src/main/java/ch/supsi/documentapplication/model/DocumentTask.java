package ch.supsi.documentapplication.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocumentTask {

    private final String id;
    private final Long documentId;
    private final TaskType type;
    private final int progress;
    private final int maxProgress;
    private final TaskVariant variant;
}
