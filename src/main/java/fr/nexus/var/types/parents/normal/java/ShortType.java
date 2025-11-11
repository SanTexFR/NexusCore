package fr.nexus.var.types.parents.normal.java;

import fr.nexus.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class ShortType extends VarType<Short>{
    //CONSTRUCTOR
    public ShortType(){
        super(Short.class,1);
    }


    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Short value){
        return addVersionToBytes(new byte[]{
                (byte)((value>>>8)&0xFF),
                (byte)(value&0xFF)
        });
    }
    public@NotNull Short deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull Short deserialize(int version,byte[]bytes){
        if(version==1)return (short)(((bytes[0]&0xFF)<<8)|(bytes[1]&0xFF));
        else throw createUnsupportedVersionException(version);
    }
}