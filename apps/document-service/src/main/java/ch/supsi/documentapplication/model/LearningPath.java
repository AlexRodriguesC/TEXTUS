package ch.supsi.documentapplication.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class LearningPath {

    private Long jobId;
    private List<Chapter> chapters;

}
