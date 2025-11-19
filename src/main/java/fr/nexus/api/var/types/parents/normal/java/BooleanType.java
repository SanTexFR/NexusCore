package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class BooleanType extends VarType<Boolean>{
    //CONSTRUCTOR
    public BooleanType(){
        super(Boolean.class,1);
    }


    //METHODS
    public byte@NotNull[]serializeSync(@NotNull Boolean value){
        return addVersionToBytes(new byte[]{(byte)(value?1:0)});
    }
    public@NotNull Boolean deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull Boolean deserialize(int version,byte[]bytes){
        if(version==1)return bytes[0]!=0;
        else throw createUnsupportedVersionException(version);
    }
}