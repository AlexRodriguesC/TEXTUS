package ch.supsi.documentapplication.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SpacePage {
    private Long id;
    private Long documentId;
    private Integer pageNumber;
    private String title;
    private String summary;
}
