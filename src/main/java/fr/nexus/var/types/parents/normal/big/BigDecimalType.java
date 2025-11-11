package fr.nexus.var.types.parents.normal.big;

import fr.nexus.var.types.VarTypes;
import fr.nexus.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class BigDecimalType extends VarType<BigDecimal>{
    //CONSTRUCTOR
    public BigDecimalType(){
        super(BigDecimal.class,1);
    }


    //METHODS
    public byte@NotNull[] serializeSync(@NotNull BigDecimal value){
        final byte[]bigIntBytes=VarTypes.BIG_INTEGER.serializeSync(value.unscaledValue());
        final ByteBuffer buffer=ByteBuffer.allocate(bigIntBytes.length+Integer.BYTES*2);
        buffer.putInt(bigIntBytes.length);
        buffer.put(bigIntBytes);
        buffer.putInt(value.scale());
        return addVersionToBytes(buffer.array());
    }
    public@NotNull BigDecimal deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull BigDecimal deserialize(int version,byte[]bytes){
        if(version==1){
            final ByteBuffer buffer=ByteBuffer.wrap(bytes);
            final byte[]bigIntBytes=new byte[buffer.getInt()];
            buffer.get(bigIntBytes);
            final BigInteger bInt=VarTypes.BIG_INTEGER.deserializeSync(bigIntBytes);
            final int scale=buffer.getInt();
            return new BigDecimal(bInt,scale);
        }else throw createUnsupportedVersionException(version);
    }
}