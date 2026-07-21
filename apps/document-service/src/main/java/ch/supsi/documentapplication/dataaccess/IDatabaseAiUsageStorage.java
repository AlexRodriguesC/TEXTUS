package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.AiUsage;

public interface IDatabaseAiUsageStorage {

    void create(String entityType, Long entityId, String operation, AiUsage usage);
}
