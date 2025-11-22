package fr.nexus.api.var.types.parents.normal.bukkit.chunk;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.normal.VarType;
import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class ChunkType extends VarType<Chunk>{
    private static final ChunkAccessor ACCESSOR = detectAccessor();
    private static ChunkAccessor detectAccessor() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.Scheduler");
            return new FoliaChunkAccessor();
        } catch (ClassNotFoundException e) {
            return new PaperChunkAccessor();
        }
    }

    //CONSTRUCTOR
    public ChunkType(){
        super(Chunk.class,1);
    }


    //METHODS

    //SYNC
    public byte@NotNull[]serializeSync(@NotNull Chunk value){
        final byte[] serializedWorld = VarTypes.WORLD.serializeSync(value.getWorld());
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + serializedWorld.length + Long.BYTES);
        buffer.putInt(serializedWorld.length);
        buffer.put(serializedWorld);
        buffer.putLong(value.getChunkKey());
        return addVersionToBytes(buffer.array());
    }
    public@NotNull Chunk deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserializeSync(var.version(),var.remainder());
    }

    private@NotNull Chunk deserializeSync(int version, byte[]bytes){
        if(version==1){
            final VersionAndRemainder var = readVersionAndRemainder(bytes);
            ByteBuffer buffer = ByteBuffer.wrap(var.remainder());
            final byte[] worldBytes = new byte[buffer.getInt()];
            buffer.get(worldBytes);
            final long chunkKey = buffer.getLong();
            return ACCESSOR.getChunkSync(worldBytes, chunkKey);
        }else throw createUnsupportedVersionException(version);
    }

    //ASYNC
    public@NotNull CompletableFuture<@NotNull Chunk>deserializeAsync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserializeAsync(var.version(),var.remainder());
    }

    private@NotNull CompletableFuture<@NotNull Chunk>deserializeAsync(int version, byte[]bytes){
        if(version==1){
            final VersionAndRemainder var = readVersionAndRemainder(bytes);
            ByteBuffer buffer = ByteBuffer.wrap(var.remainder());
            final byte[] worldBytes = new byte[buffer.getInt()];
            buffer.get(worldBytes);
            final long chunkKey = buffer.getLong();
            return ACCESSOR.getChunkAsync(worldBytes, chunkKey);
        }else throw createUnsupportedVersionException(version);
    }
}