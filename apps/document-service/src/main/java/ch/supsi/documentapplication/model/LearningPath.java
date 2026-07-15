package ch.supsi.documentapplication.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LearningPath {

    private Long jobId;
    private PathSettings settings;
    private List<Chapter> chapters;
}
