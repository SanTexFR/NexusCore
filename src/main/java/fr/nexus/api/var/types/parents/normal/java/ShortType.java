package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class ShortType extends VarType<Short>{
    //CONSTRUCTOR
    public ShortType(){
        super(Short.class,1);
    }


    //METHODS
    public byte@NotNull[]serializeSync(@NotNull Short value){
        final int zig=(value<<1)^(value>>15);
        return addVersionToBytes(IntegerType.toVarInt(zig));
    }
    public@NotNull Short deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull Short deserialize(int version,byte[]bytes){
        if (version != 1)
            throw createUnsupportedVersionException(version);

        final int[]res=IntArrayType.fromVarIntWithOffset(bytes,0);
        int zig=res[0];
        return(short)((zig>>>1)^-(zig&1));
    }
}