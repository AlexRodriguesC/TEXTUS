package ch.supsi.textusbackend.dataaccess;

import ch.supsi.textusbackend.model.BookIndexEntrySummary;

public interface SummaryStorage {

    Long createSummary(BookIndexEntrySummary bookIndexEntrySummary, Long bookId, Long indexId);
}
