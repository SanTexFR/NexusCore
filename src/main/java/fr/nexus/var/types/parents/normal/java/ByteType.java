package fr.nexus.var.types.parents.normal.java;

import fr.nexus.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class ByteType extends VarType<Byte>{
    //CONSTRUCTOR
    public ByteType(){
        super(Byte.class,1);
    }


    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Byte value){
        return addVersionToBytes(new byte[]{value});
    }
    public@NotNull Byte deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull Byte deserialize(int version,byte[]bytes){
        if(version==1)return bytes[0];
        else throw createUnsupportedVersionException(version);
    }
}