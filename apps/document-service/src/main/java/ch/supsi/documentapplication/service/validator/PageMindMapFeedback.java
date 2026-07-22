package ch.supsi.documentapplication.service.validator;

import java.util.List;
import java.util.Objects;
import lombok.ToString;

@ToString
public class PageMindMapFeedback {

    private final List<Long> missingPageIds;

    @ToString.Exclude
    private final List<Long> duplicatePageIds;

    @ToString.Exclude
    private final List<Long> extraPageIds;

    public PageMindMapFeedback(List<Long> missingPageIds, List<Long> duplicatePageIds, List<Long> extraPageIds) {
        this.missingPageIds = missingPageIds;
        this.duplicatePageIds = duplicatePageIds;
        this.extraPageIds = extraPageIds;
    }

    public List<Long> missingPageIds() {
        return missingPageIds;
    }

    public List<Long> duplicatePageIds() {
        return duplicatePageIds;
    }

    public List<Long> extraPageIds() {
        return extraPageIds;
    }

    public boolean isValid() {
        return missingPageIds.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PageMindMapFeedback that)) return false;
        return Objects.equals(missingPageIds, that.missingPageIds)
                && Objects.equals(duplicatePageIds, that.duplicatePageIds)
                && Objects.equals(extraPageIds, that.extraPageIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(missingPageIds, duplicatePageIds, extraPageIds);
    }
}
