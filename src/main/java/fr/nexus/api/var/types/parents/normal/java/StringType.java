package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.VarType;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class StringType extends VarType<String>{
    //METHODS
    public byte@NotNull[] serializeSync(@NotNull String str){
        return addVersionToBytes(str.getBytes(StandardCharsets.UTF_8));
    }
    public@NotNull String deserializeSync(int version,byte[]bytes){
        if(version==1)return new String(bytes,StandardCharsets.UTF_8);
        else throw createUnsupportedVersionException(version);
    }
}