package ch.supsi.documentapplication.model;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Document {

    private final String name;
    private final Long id;
    private final Long jobId;

    @Setter
    private DocumentStatus status;

    public Document(String name, Long id, Long jobId) {
        this.name = name;
        this.id = id;
        this.jobId = jobId;
    }
}
