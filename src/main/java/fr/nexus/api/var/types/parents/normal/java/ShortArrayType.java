package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.VarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class ShortArrayType extends VarType<short[]>{
    @Override
    public byte@NotNull[]serializeSync(short @NotNull[]value){
        byte[]lenBytes=IntegerType.toVarInt(value.length);

        byte[][]encodedShorts=new byte[value.length][];
        int totalSize=lenBytes.length;

        for(int i=0;i<value.length;i++){
            final int zig=(value[i]<<1)^(value[i]>>15);
            encodedShorts[i]=IntegerType.toVarInt(zig);
            totalSize+=encodedShorts[i].length;
        }

        final byte[]out=new byte[totalSize];
        int pos=0;

        System.arraycopy(lenBytes,0,out,pos,lenBytes.length);
        pos+=lenBytes.length;

        for(final byte[]b:encodedShorts){
            System.arraycopy(b,0,out,pos,b.length);
            pos+=b.length;
        }

        return addVersionToBytes(out);
    }

    @Override
    public short@NotNull[]deserializeSync(int version,byte[]bytes){
        if(version!=1)throw createUnsupportedVersionException(version);

        int offset=0;

        final int[]lenRes=IntArrayType.fromVarIntWithOffset(bytes,offset);
        int length=lenRes[0];
        offset=lenRes[1];

        short[] result = new short[length];

        for(int i=0;i<length;i++){
            final int[]sr=IntArrayType.fromVarIntWithOffset(bytes,offset);
            final int zig=sr[0];
            result[i]=(short)((zig>>>1)^-(zig&1));
            offset=sr[1];
        }

        return result;
    }
}