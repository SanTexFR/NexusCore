package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class BooleanArrayType extends VarType<boolean[]>{
    //CONSTRUCTOR
    public BooleanArrayType(){
        super(boolean[].class,1);
    }


    //METHODS
    public byte@NotNull[]serializeSync(boolean@NotNull[]value){
        final int length=value.length;
        final int byteCount=(length+7)/8;

        final byte[]data=new byte[4+byteCount];

        data[0]=(byte)(length>>>24);
        data[1]=(byte)(length>>>16);
        data[2]=(byte)(length>>>8);
        data[3]=(byte)length;

        int pos=4;
        for(int i=0;i<byteCount;i++){
            byte b=0;
            for(int bit=0;bit<8;bit++){

                final int idx=i*8+bit;
                if(idx>=length)break;

                if(value[idx])
                    b=(byte)(b|(1<<bit));
            }
            data[pos++]=b;
        }

        return addVersionToBytes(data);
    }
    public boolean@NotNull[]deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private boolean@NotNull[]deserialize(int version,byte[]bytes){
        if(version!=1)throw createUnsupportedVersionException(version);

        final int length=((bytes[0]&0xFF)<<24)|
                        ((bytes[1]&0xFF)<<16)|
                        ((bytes[2]&0xFF)<<8)|
                        (bytes[3]&0xFF);

        final boolean[]result=new boolean[length];

        final int byteCount=(length+7)/8;

        final int pos=4;
        for(int i=0;i<length;i++){
            final int byteIndex=i/8;
            final int bitIndex=i%8;

            final byte b=bytes[pos+byteIndex];

            result[i]=((b>>bitIndex)&1)==1;
        }

        return result;
    }
}