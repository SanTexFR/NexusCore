package fr.nexus.api.var.types.parents.normal.bukkit;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.InternalVarType;
import fr.nexus.api.var.types.parents.normal.java.IntegerType; // Import
import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class ChunkType extends InternalVarType<Chunk> {

    // SYNC
    @Override
    public byte @NotNull [] serializeSync(@NotNull Chunk value) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] serializedWorld = VarTypes.WORLD.serializeSync(value.getWorld());

            IntegerType.writeVarInt(baos, serializedWorld.length);
            baos.write(serializedWorld);

            IntegerType.writeVarInt(baos, value.getX());
            IntegerType.writeVarInt(baos, value.getZ());

            return addVersionToBytes(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Chunk deserializeSync(int version, byte[] bytes) {
        if (version == 1) {
            return deserializeAsync(version, bytes).join();
        } else throw createUnsupportedVersionException(version);
    }

    // ASYNC
    @Override
    public @NotNull CompletableFuture<@NotNull Chunk> deserializeAsync(byte @NotNull [] bytes) {
        final VersionAndRemainder value = readVersionAndRemainder(bytes);
        return deserializeAsync(value.version(), value.remainder());
    }

    private @NotNull CompletableFuture<@NotNull Chunk> deserializeAsync(int version, byte[] bytes) {
        if (version == 1) {
            final ByteBuffer buffer = ByteBuffer.wrap(bytes);

            // Lecture World
            int worldLen = IntegerType.fromVarInt(buffer);
            byte[] worldBytes = new byte[worldLen];
            buffer.get(worldBytes);

            final int x = IntegerType.fromVarInt(buffer);
            final int z = IntegerType.fromVarInt(buffer);

            return VarTypes.WORLD.deserializeAsync(worldBytes)
                    .thenCompose(world -> {
                        if (world != null) return world.getChunkAtAsync(x, z);
                        CompletableFuture<Chunk> failed = new CompletableFuture<>();
                        failed.completeExceptionally(new NullPointerException("Deserialized world is null"));
                        return failed;
                    });
        } else throw createUnsupportedVersionException(version);
    }
}