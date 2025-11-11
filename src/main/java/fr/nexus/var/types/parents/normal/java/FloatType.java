package fr.nexus.var.types.parents.normal.java;

import fr.nexus.var.types.VarTypes;
import fr.nexus.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class FloatType extends VarType<Float>{
    //CONSTRUCTOR
    public FloatType(){
        super(Float.class,1);
    }


    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Float value){
        return addVersionToBytes(VarTypes.INTEGER.serializeSync(Float.floatToIntBits(value)));
    }
    public@NotNull Float deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull Float deserialize(int version,byte[]bytes){
        if(version==1)return Float.intBitsToFloat(VarTypes.INTEGER.deserializeSync(bytes));
        else throw createUnsupportedVersionException(version);
    }
}