package ch.supsi.documentapplication.model.learn.exercise;

import java.util.List;
import java.util.Map;

public record GroupAnswer(Map.Entry<String, List<String>> group, String explanation) {
}
