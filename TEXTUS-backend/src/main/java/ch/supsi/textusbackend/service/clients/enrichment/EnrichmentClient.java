package ch.supsi.textusbackend.service.clients.enrichment;

import ch.supsi.textusbackend.model.Chunk;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EnrichmentClient {

    Chunk enrich(@Nullable String previousSummary, @NotNull String toEnrich, @NotNull List<String> rollingKeys);
}
