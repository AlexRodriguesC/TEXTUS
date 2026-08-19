package ch.supsi.documentapplication.model;

import java.util.List;

public record ClientResult<O>(O result, List<AiUsage> usages, List<String> usageOperations) {

    public ClientResult(O result, List<AiUsage> usages) {
        this(result, usages, null);
    }

    public ClientResult(O result, AiUsage usage) {
        this(result, List.of(usage), null);
    }
}
