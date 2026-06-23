package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.Chunk;

public interface IDatabaseDocumentStorage {

    Long create(Chunk chunk, Long spaceId);

    Chunk read(Long chunkId);
}
