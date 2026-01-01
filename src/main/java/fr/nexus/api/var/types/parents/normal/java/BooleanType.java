package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.VarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class BooleanType extends VarType<Boolean>{
    //METHODS
    public byte@NotNull[]serializeSync(@NotNull Boolean value){
        return addVersionToBytes(new byte[]{(byte)(value?1:0)});
    }
    public@NotNull Boolean deserializeSync(int version,byte[]bytes){
        if(version==1)return bytes[0]!=0;
        else throw createUnsupportedVersionException(version);
    }
}