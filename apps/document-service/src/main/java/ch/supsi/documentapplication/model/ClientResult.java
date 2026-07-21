package ch.supsi.documentapplication.model;

import java.util.List;

public record ClientResult<O>(O result, List<AiUsage> usages) {

    public ClientResult(O result, AiUsage usage) {
        this(result, List.of(usage));
    }
}
