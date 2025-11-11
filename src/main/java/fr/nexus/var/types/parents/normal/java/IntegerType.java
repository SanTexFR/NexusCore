package fr.nexus.var.types.parents.normal.java;

import fr.nexus.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class IntegerType extends VarType<Integer>{
    //CONSTRUCTOR
    public IntegerType(){
        super(Integer.class,1);
    }


    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Integer value){
        final int v=value;
        return addVersionToBytes(new byte[]{
                (byte)(v>>>24),
                (byte)(v>>>16),
                (byte)(v>>>8),
                (byte)v
        });
    }
    public@NotNull Integer deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull Integer deserialize(int version,byte[]bytes){
        if(version==1){
            return((bytes[0]&0xFF)<<24)|
                    ((bytes[1]&0xFF)<<16)|
                    ((bytes[2]&0xFF)<<8)|
                    (bytes[3]&0xFF);
        }else throw createUnsupportedVersionException(version);
    }
}