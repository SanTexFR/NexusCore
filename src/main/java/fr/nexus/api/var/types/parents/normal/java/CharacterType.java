package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.VarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class CharacterType extends VarType<Character>{
    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Character value){
        final char c=value;
        return addVersionToBytes(new byte[]{
                (byte)(c>>>8),
                (byte)c
        });
    }
    public@NotNull Character deserializeSync(int version, byte[]bytes){
        if(version==1)
            return(char)(((bytes[0]&0xFF)<<8)|
                    (bytes[1]&0xFF));
        else throw createUnsupportedVersionException(version);
    }
}