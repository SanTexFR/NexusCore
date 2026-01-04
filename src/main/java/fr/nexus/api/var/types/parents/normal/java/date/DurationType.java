package fr.nexus.api.var.types.parents.normal.java.date;

import fr.nexus.api.var.types.parents.InternalVarType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.time.Duration;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class DurationType extends InternalVarType<Duration>{
    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Duration value){
        final ByteBuffer buffer=ByteBuffer.allocate(Long.BYTES*2);
        buffer.putLong(value.getSeconds());
        buffer.putInt(value.getNano());
        return addVersionToBytes(buffer.array());
    }
    public@NotNull Duration deserializeSync(int version,byte[]bytes){
        if(version==1){
            final ByteBuffer buffer=ByteBuffer.wrap(bytes);
            return Duration.ofSeconds(buffer.getLong(),buffer.getInt());
        } else throw createUnsupportedVersionException(version);
    }
}