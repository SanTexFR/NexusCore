package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.InternalVarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class DoubleType extends InternalVarType<Double>{
    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Double value){
        final long longBits=Double.doubleToLongBits(value);
        return addVersionToBytes(new byte[]{
                (byte)(longBits>>>56),
                (byte)(longBits>>>48),
                (byte)(longBits>>>40),
                (byte)(longBits>>>32),
                (byte)(longBits>>>24),
                (byte)(longBits>>>16),
                (byte)(longBits>>>8),
                (byte)longBits
        });
    }
    public@NotNull Double deserializeSync(int version,byte[]bytes){
        if(version==1){
            final long longBits=((long)(bytes[0]&0xFF)<<56)|
                    ((long)(bytes[1]&0xFF)<<48)|
                    ((long)(bytes[2]&0xFF)<<40)|
                    ((long)(bytes[3]&0xFF)<<32)|
                    ((long)(bytes[4]&0xFF)<<24)|
                    ((long)(bytes[5]&0xFF)<<16)|
                    ((long)(bytes[6]&0xFF)<<8)|
                    ((long)(bytes[7]&0xFF));
            return Double.longBitsToDouble(longBits);
        } else throw createUnsupportedVersionException(version);
    }
}