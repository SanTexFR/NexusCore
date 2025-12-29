package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class LongArrayType extends VarType<long[]>{
    @Override
    public byte@NotNull[]serializeSync(long @NotNull[]value) {
        final byte[]lenBytes=IntegerType.toVarInt(value.length);

        int total=lenBytes.length;
        final byte[][]encoded=new byte[value.length][];

        for(int i=0;i<value.length;i++){
            encoded[i]=LongType.toVarLong(value[i]);
            total+=encoded[i].length;
        }

        final byte[]out=new byte[total];
        int pos=0;

        System.arraycopy(lenBytes,0,out,pos,lenBytes.length);
        pos+=lenBytes.length;

        for(final byte[]e:encoded){
            System.arraycopy(e,0,out,pos,e.length);
            pos+=e.length;
        }

        return addVersionToBytes(out);
    }

    public long @NotNull[]deserializeSync(int version,byte[]bytes){
        if(version!=1)throw createUnsupportedVersionException(version);

        int offset=0;

        final int[]lenResult=IntArrayType.fromVarIntWithOffset(bytes,offset);
        int length=lenResult[0];
        offset=lenResult[1];

        final long[]result=new long[length];
        for(int i=0;i<length;i++){
            long[]lr=fromVarLongWithOffset(bytes,offset);
            result[i]=lr[0];
            offset=(int)lr[1];
        }

        return result;
    }

    public static long[]fromVarLongWithOffset(byte[]bytes,int offset){
        long value=0;
        int position=0;
        int index=offset;

        while(index<bytes.length){
            final byte b=bytes[index++];

            value|=(long)(b&0x7F)<<position;

            if((b&0x80)==0)
                return new long[]{value,index};

            position+=7;

            if(position>=64)
                throw new RuntimeException("VarLong trop long");
        }

        throw new IllegalArgumentException("VarLong invalide ou tronqué");
    }

}