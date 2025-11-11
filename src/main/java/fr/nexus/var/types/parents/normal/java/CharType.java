package fr.nexus.var.types.parents.normal.java;

import fr.nexus.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class CharType extends VarType<Character>{
    //CONSTRUCTOR
    public CharType(){
        super(Character.class,1);
    }


    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Character value){
        final char c=value;
        return addVersionToBytes(new byte[]{
                (byte)(c>>>8),
                (byte)c
        });
    }
    public@NotNull Character deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull Character deserialize(int version, byte[]bytes){
        if(version==1)
            return(char)(((bytes[0]&0xFF)<<8)|
                    (bytes[1]&0xFF));
        else throw createUnsupportedVersionException(version);
    }
}