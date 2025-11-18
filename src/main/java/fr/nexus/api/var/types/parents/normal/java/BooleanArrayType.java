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

        int length = value.length;
        int packedLength = (length + 7) / 8; // nombre de bytes nécessaires
        byte[] packed = new byte[packedLength + 4]; // +4 bytes pour stocker la taille originale

        // stocker la longueur dans les 4 premiers bytes
        packed[0] = (byte) (length >>> 24);
        packed[1] = (byte) (length >>> 16);
        packed[2] = (byte) (length >>> 8);
        packed[3] = (byte) (length);

        int index = 4;

        for (int i = 0; i < length; i++) {
            if (value[i]) {
                packed[index] = (byte) (packed[index] | (1 << (i % 8)));
            }
            if (i % 8 == 7) index++;
        }

        return addVersionToBytes(packed);
    }
    public boolean@NotNull[]deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private boolean@NotNull[]deserialize(int version,byte[]bytes){
        if(version!=1)throw createUnsupportedVersionException(version);
        final int length= ((bytes[0] & 0xFF) << 24) |
                        ((bytes[1] & 0xFF) << 16) |
                        ((bytes[2] & 0xFF) << 8) |
                        (bytes[3] & 0xFF);

        boolean[] result = new boolean[length];
        int index = 4;
        for (int i = 0; i < length; i++) {
            result[i] = (bytes[index] & (1 << (i % 8))) != 0;
            if (i % 8 == 7) index++;
        }
        return result;
    }
}