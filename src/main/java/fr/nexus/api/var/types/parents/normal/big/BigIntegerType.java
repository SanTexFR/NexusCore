package fr.nexus.api.var.types.parents.normal.big;

import fr.nexus.api.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class BigIntegerType extends VarType<BigInteger>{
    //METHODS
    public byte@NotNull[] serializeSync(@NotNull BigInteger value){
        return addVersionToBytes(value.toByteArray());
    }
    public@NotNull BigInteger deserializeSync(int version, byte[]bytes){
        if(version==1)return new BigInteger(bytes);
        else throw createUnsupportedVersionException(version);
    }
}