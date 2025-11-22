package fr.nexus.api.var.types.parents.normal.bukkit.chunk;

import fr.nexus.api.var.types.VarTypes;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@SuppressWarnings({"unused","UnusedReturnValue"})

public class FoliaChunkAccessor implements ChunkAccessor {
    private final Object scheduler;
    private final Method executeSyncMethod;

    public FoliaChunkAccessor() {
        try {
            Class<?> schedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.Scheduler");
            Method getScheduler = Bukkit.getServer().getClass().getMethod("getScheduler");
            this.scheduler = getScheduler.invoke(Bukkit.getServer());
            this.executeSyncMethod = schedulerClass.getMethod("executeSync", Callable.class);
        } catch (Exception e) {
            throw new RuntimeException("Folia classes not found", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T runSync(Callable<T> task) {
        try {
            Future<T> future = (Future<T>) executeSyncMethod.invoke(scheduler, task);
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Chunk getChunkSync(byte[] serializedWorld, long chunkKey) {
        return runSync(() -> VarTypes.WORLD.deserializeSync(serializedWorld)
                .getChunkAt((int) chunkKey, (int) (chunkKey >> 32)));
    }

    @Override
    public CompletableFuture<Chunk> getChunkAsync(byte[] serializedWorld, long chunkKey) {
        return VarTypes.WORLD.deserializeAsync(serializedWorld)
                .thenCompose(world -> {
                    if (world == null) {
                        CompletableFuture<Chunk> failed = new CompletableFuture<>();
                        failed.completeExceptionally(new NullPointerException("Deserialized world is null"));
                        return failed;
                    }
                    // Ici on exécute le getChunkAt sur le thread principal via runSync
                    return CompletableFuture.supplyAsync(() -> runSync(
                            () -> world.getChunkAt((int) chunkKey, (int) (chunkKey >> 32))
                    ));
                });
    }
}