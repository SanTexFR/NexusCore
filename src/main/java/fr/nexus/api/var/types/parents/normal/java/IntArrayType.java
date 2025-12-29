package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class IntArrayType extends VarType<int[]>{
    @Override
    public byte@NotNull[]serializeSync(int @NotNull[]value){
        final byte[]len=IntegerType.toVarInt(value.length);

        final byte[]temp=new byte[len.length+value.length*5];
        int pos=0;

        System.arraycopy(len,0,temp,pos,len.length);
        pos+=len.length;

        for(final int v:value){
            byte[]vi=IntegerType.toVarInt(v);
            System.arraycopy(vi,0,temp,pos,vi.length);
            pos+=vi.length;
        }

        final byte[]result=new byte[pos];
        System.arraycopy(temp,0,result,0,pos);

        return addVersionToBytes(result);
    }


    public int@NotNull[]deserializeSync(int version,byte[]bytes){
        if(version!=1)
            throw createUnsupportedVersionException(version);

        int index=0;

        final int[]lenRead=fromVarIntWithOffset(bytes,index);
        final int length=lenRead[0];
        index=lenRead[1];

        final int[]result=new int[length];
        for(int i=0;i<length;i++){
            int[]v=fromVarIntWithOffset(bytes,index);
            result[i]=v[0];
            index=v[1];
        }

        return result;
    }
    public static int[]fromVarIntWithOffset(byte[]bytes,int offset){
        int value=0;
        int position=0;
        int index=offset;

        while(index<bytes.length){
            final byte b=bytes[index++];

            value|=(b&0x7F)<<position;

            if((b&0x80)==0)
                return new int[]{value,index};

            position+=7;

            if(position>=32)
                throw new RuntimeException("VarInt trop long");
        }

        throw new IllegalArgumentException("VarInt invalide ou tronqué");
    }
}