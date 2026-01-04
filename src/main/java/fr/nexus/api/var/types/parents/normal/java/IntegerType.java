package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.InternalVarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class IntegerType extends InternalVarType<Integer>{
    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Integer value){
        return addVersionToBytes(toVarInt(zigZagEncode(value)));
    }
    public@NotNull Integer deserializeSync(int version,byte[]bytes){
        if(version==1){
            return zigZagDecode(fromVarInt(bytes));
        }else throw createUnsupportedVersionException(version);
    }

    //INT UTILS
    public static int zigZagEncode(int n){
        return(n<<1)^(n>>31);
    }

    public static int zigZagDecode(int n){
        return (n>>>1)^-(n&1);
    }

    public static byte[]toVarInt(int value){
        final byte[]buffer=new byte[5];
        int index=0;

        while((value&~0x7F)!=0){
            buffer[index++]=(byte)((value&0x7F)|0x80);
            value>>>=7;
        }

        buffer[index++]=(byte)value;

        final byte[]result=new byte[index];
        System.arraycopy(buffer,0,result,0,index);

        return result;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public static int fromVarInt(byte[] bytes) {
        int value=0;
        int position=0;
        for(int i=0;i<bytes.length;i++){
            final byte b=bytes[i];

            value|=(b&0x7F)<<position;

            if((b&0x80)==0)
                return value;

            position+=7;

            if(position>=32)
                throw new RuntimeException("VarInt trop long");
        }

        throw new IllegalArgumentException("VarInt invalide ou tronqué");
    }
}