package ch.supsi.documentapplication.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Document {

    private Long id;
    private DocumentStatus status;
}
