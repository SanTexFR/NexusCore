package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class LongType extends VarType<Long>{
    //CONSTRUCTOR
    public LongType(){
        super(Long.class,1);
    }


    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Long value){
        final byte[]bytes=new byte[8];
        for(int i=0;i<8;i++)
            bytes[i]=(byte)(value>>>(8*(7-i)));
        return addVersionToBytes(bytes);
    }
    public@NotNull Long deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull Long deserialize(int version,byte[]bytes){
        if(version==1){
            long value=0;
            for(int i=0;i<8;i++)
                value=(value<<8)|(bytes[i]&0xFF);
            return value;
        }else throw createUnsupportedVersionException(version);
    }
}