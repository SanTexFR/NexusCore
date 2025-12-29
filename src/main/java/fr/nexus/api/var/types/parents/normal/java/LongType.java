package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class LongType extends VarType<Long>{
    @Override
    public byte@NotNull[]serializeSync(@NotNull Long value){
        final byte[]encoded=toVarLong(value);
        return addVersionToBytes(encoded);
    }

    public@NotNull Long deserializeSync(int version,byte[]bytes){
        if(version!=1)throw createUnsupportedVersionException(version);

        return fromVarLong(bytes);
    }

    public static byte[]toVarLong(long value) {
        final byte[]buffer=new byte[10];
        int index=0;
        while((value&~0x7FL)!=0){
            buffer[index++]=(byte)((value&0x7FL)|0x80L);
            value>>>=7;
        }

        buffer[index++]=(byte)value;

        final byte[]result=new byte[index];
        System.arraycopy(buffer,0,result,0,index);
        return result;
    }

    public static long fromVarLong(byte[]bytes){
        long value=0;
        int position=0;

        for(int i=0;i<bytes.length;i++){
            long b=bytes[i]&0x7FL;
            value|=(b<<position);

            if((bytes[i]&0x80)==0)
                return value;

            position+=7;

            if(position>=64)throw new RuntimeException("VarLong trop long");
        }

        throw new IllegalArgumentException("VarLong invalide ou tronqué");
    }
}