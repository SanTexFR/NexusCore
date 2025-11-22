package fr.nexus.api.var.types.parents.normal.bukkit.chunk;

import org.bukkit.Chunk;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"unused","UnusedReturnValue"})
public interface ChunkAccessor {
    Chunk getChunkSync(byte[] serializedWorld, long chunkKey);
    CompletableFuture<Chunk> getChunkAsync(byte[] serializedWorld, long chunkKey);
}