package fr.nexus.var.types.parents.normal.big;

import fr.nexus.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class BigIntegerType extends VarType<BigInteger>{
    //CONSTRUCTOR
    public BigIntegerType(){
        super(BigInteger.class,1);
    }


    //METHODS
    public byte@NotNull[] serializeSync(@NotNull BigInteger value){
        return addVersionToBytes(value.toByteArray());
    }
    public@NotNull BigInteger deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull BigInteger deserialize(int version, byte[]bytes){
        if(version==1)return new BigInteger(bytes);
        else throw createUnsupportedVersionException(version);
    }
}