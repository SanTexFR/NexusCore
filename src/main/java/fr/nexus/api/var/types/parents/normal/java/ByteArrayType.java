package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.InternalVarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class ByteArrayType extends InternalVarType<byte[]>{
    //METHODS
    public byte@NotNull[]serializeSync(byte@NotNull[]value){
        return addVersionToBytes(value);
    }
    public byte@NotNull[]deserializeSync(int version,byte[]bytes){
        if(version==1)return bytes;
        else throw createUnsupportedVersionException(version);
    }
}