package ch.supsi.documentapplication.model;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Document {

    private Long id;
    private Long jobId;

    @Setter
    private DocumentStatus status;

    public Document(Long id, Long jobId) {
        this.id = id;
        this.jobId = jobId;
    }
}
