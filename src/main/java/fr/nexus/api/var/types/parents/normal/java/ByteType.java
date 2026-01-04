package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.InternalVarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class ByteType extends InternalVarType<Byte>{
    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Byte value){
        return addVersionToBytes(new byte[]{value});
    }
    public@NotNull Byte deserializeSync(int version,byte[]bytes){
        if(version==1)return bytes[0];
        else throw createUnsupportedVersionException(version);
    }
}