//package fr.nexus.api.var.types.parents.normal.bukkit.chunk;
//
//import fr.nexus.api.var.types.VarTypes;
//import org.bukkit.Chunk;
//
//import java.util.concurrent.CompletableFuture;
//
//@SuppressWarnings({"unused","UnusedReturnValue"})
//public class PaperChunkAccessor implements ChunkAccessor {
//    @Override
//    public Chunk getChunkSync(byte[] serializedWorld, long chunkKey) {
//        return VarTypes.WORLD.deserializeSync(serializedWorld).getChunkAt((int) chunkKey, (int) (chunkKey >> 32));
//    }
//
//    @Override
//    public CompletableFuture<Chunk> getChunkAsync(byte[] serializedWorld, long chunkKey) {
//        return VarTypes.WORLD.deserializeAsync(serializedWorld)
//                .thenCompose(world -> {
//                    if (world != null) return world.getChunkAtAsync((int) chunkKey, (int) (chunkKey >> 32));
//                    CompletableFuture<Chunk> failed = new CompletableFuture<>();
//                    failed.completeExceptionally(new NullPointerException("Deserialized world is null"));
//                    return failed;
//                });
//    }
//}