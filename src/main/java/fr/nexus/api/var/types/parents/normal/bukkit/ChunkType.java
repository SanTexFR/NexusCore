package fr.nexus.api.var.types.parents.normal.bukkit;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.normal.VarType;
import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class ChunkType extends VarType<Chunk>{
    //CONSTRUCTOR
    public ChunkType(){
        super(Chunk.class,1);
    }


    //METHODS

    //SYNC
    public byte@NotNull[]serializeSync(@NotNull Chunk value){
        final byte[]serializedWorld=VarTypes.WORLD.serializeSync(value.getWorld());
        final ByteBuffer buffer=ByteBuffer.allocate(Integer.BYTES+serializedWorld.length+Long.BYTES);

        //WORLD
        buffer.putInt(serializedWorld.length);
        buffer.put(serializedWorld);

        //KEY
        buffer.putLong(value.getChunkKey());

        return addVersionToBytes(buffer.array());
    }
    public@NotNull Chunk deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserializeSync(var.version(),var.remainder());
    }

    private@NotNull Chunk deserializeSync(int version, byte[]bytes){
        if(version==1){
            return deserializeAsync(version,bytes).join();
        }else throw createUnsupportedVersionException(version);
    }

    //ASYNC
    public@NotNull CompletableFuture<@NotNull Chunk>deserializeAsync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserializeAsync(var.version(),var.remainder());
    }

    private@NotNull CompletableFuture<@NotNull Chunk>deserializeAsync(int version, byte[]bytes){
        if(version==1){
            final ByteBuffer buffer=ByteBuffer.wrap(bytes);
            final byte[]worldBytes=new byte[buffer.getInt()];
            buffer.get(worldBytes);

            final long chunkKey=buffer.getLong();
            final int x=(int)chunkKey;
            final int z=(int)(chunkKey>>32);

            return VarTypes.WORLD.deserializeAsync(worldBytes)
                    .thenCompose(world->{
                        if(world!=null)return world.getChunkAtAsync(x,z);

                        final CompletableFuture<Chunk>failed=new CompletableFuture<>();
                        failed.completeExceptionally(new NullPointerException("Deserialized world is null"));
                        return failed;
                    });
        }else throw createUnsupportedVersionException(version);
    }
}