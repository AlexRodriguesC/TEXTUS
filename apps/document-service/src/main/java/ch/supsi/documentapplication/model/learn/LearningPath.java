package ch.supsi.documentapplication.model.learn;

import ch.supsi.documentapplication.model.PathSettings;
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
