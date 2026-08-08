package ch.supsi.documentapplication.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Document {

    private final String name;
    private final Long id;
    private final Long jobId;
    private final int orderIndex;

    @Setter
    private List<DocumentTask> tasks;

    public Document(String name, Long id, Long jobId, int orderIndex) {
        this.name = name;
        this.id = id;
        this.jobId = jobId;
        this.orderIndex = orderIndex;
    }
}
