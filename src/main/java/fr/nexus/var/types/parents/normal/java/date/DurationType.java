package fr.nexus.var.types.parents.normal.java.date;

import fr.nexus.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.time.Duration;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class DurationType extends VarType<Duration>{
    //CONSTRUCTOR
    public DurationType(){
        super(Duration.class,1);
    }


    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Duration value){
        final ByteBuffer buffer=ByteBuffer.allocate(Long.BYTES*2);
        buffer.putLong(value.getSeconds());
        buffer.putInt(value.getNano());
        return addVersionToBytes(buffer.array());
    }
    public@NotNull Duration deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull Duration deserialize(int version,byte[]bytes){
        if(version==1){
            final ByteBuffer buffer=ByteBuffer.wrap(bytes);
            return Duration.ofSeconds(buffer.getLong(),buffer.getInt());
        } else throw createUnsupportedVersionException(version);
    }
}