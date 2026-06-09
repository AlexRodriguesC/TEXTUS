package ch.supsi.textusbackend.dataaccess;

import ch.supsi.textusbackend.model.Chunk;

public interface IDatabaseDocumentStorage {

    Long create(Chunk chunk);

    Chunk read(Long chunkId);
}
