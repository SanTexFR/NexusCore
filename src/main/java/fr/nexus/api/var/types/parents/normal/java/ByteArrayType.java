package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class ByteArrayType extends VarType<byte[]>{
    //CONSTRUCTOR
    public ByteArrayType(){
        super(byte[].class,1);
    }


    //METHODS
    public byte@NotNull[]serializeSync(byte@NotNull[]value){
        return addVersionToBytes(value);
    }
    public byte@NotNull[]deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private byte@NotNull[]deserialize(int version,byte[]bytes){
        if(version==1)return bytes;
        else throw createUnsupportedVersionException(version);
    }
}