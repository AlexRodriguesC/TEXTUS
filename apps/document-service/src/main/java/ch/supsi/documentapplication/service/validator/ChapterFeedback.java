package ch.supsi.documentapplication.service.validator;

import java.util.List;
import java.util.Objects;
import lombok.ToString;

@ToString
public class ChapterFeedback {

    private final List<Long> missingLabelIds;

    @ToString.Exclude
    private final List<Long> duplicateLabelIds;

    @ToString.Exclude
    private final List<Long> extraLabelIds;

    public ChapterFeedback(List<Long> missingLabelIds, List<Long> duplicateLabelIds, List<Long> extraLabelIds) {
        this.missingLabelIds = missingLabelIds;
        this.duplicateLabelIds = duplicateLabelIds;
        this.extraLabelIds = extraLabelIds;
    }

    public List<Long> missingLabelIds() {
        return missingLabelIds;
    }

    public List<Long> duplicateLabelIds() {
        return duplicateLabelIds;
    }

    public List<Long> extraLabelIds() {
        return extraLabelIds;
    }

    public boolean isValid() {
        return missingLabelIds.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChapterFeedback that)) return false;
        return Objects.equals(missingLabelIds, that.missingLabelIds)
                && Objects.equals(duplicateLabelIds, that.duplicateLabelIds)
                && Objects.equals(extraLabelIds, that.extraLabelIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(missingLabelIds, duplicateLabelIds, extraLabelIds);
    }
}
