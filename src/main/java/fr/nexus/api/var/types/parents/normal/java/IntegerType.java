package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.normal.VarType;
import fr.nexus.utils.VarIntUtils;
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
        return addVersionToBytes(VarIntUtils.toVarInt(VarIntUtils.zigZagEncode(v)));
    }
    public@NotNull Integer deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull Integer deserialize(int version,byte[]bytes){
        if(version==1){
            return VarIntUtils.zigZagDecode(VarIntUtils.fromVarInt(bytes));
        }else throw createUnsupportedVersionException(version);
    }
}