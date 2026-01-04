package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.InternalVarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class FloatType extends InternalVarType<Float>{
    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Float value){
        return addVersionToBytes(VarTypes.INTEGER.serializeSync(Float.floatToIntBits(value)));
    }
    public@NotNull Float deserializeSync(int version,byte[]bytes){
        if(version==1)return Float.intBitsToFloat(VarTypes.INTEGER.deserializeSync(bytes));
        else throw createUnsupportedVersionException(version);
    }
}