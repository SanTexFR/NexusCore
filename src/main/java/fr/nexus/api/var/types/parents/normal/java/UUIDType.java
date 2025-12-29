package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class UUIDType extends VarType<UUID>{
    //METHODS
    public byte@NotNull[]serializeSync(@NotNull UUID uuid){
        final byte[]bytes=new byte[16];
        final long most=uuid.getMostSignificantBits(),
                least=uuid.getLeastSignificantBits();

        for(int i=0;i<8;i++){
            bytes[i]=(byte)(most>>>(8*(7-i)));
            bytes[8+i]=(byte)(least>>>(8*(7-i)));
        }return addVersionToBytes(bytes);
    }
    public@NotNull UUID deserializeSync(int version,byte[]bytes){
        if(version==1){
            long most=0,least=0;
            for(int i=0;i<8;i++){
                most=(most<<8)|(bytes[i]&0xFF);
                least=(least<<8)|(bytes[8+i]&0xFF);
            }return new UUID(most,least);
        }else throw createUnsupportedVersionException(version);
    }
}