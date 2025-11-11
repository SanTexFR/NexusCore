package fr.nexus.var.types.parents.normal.java;

import fr.nexus.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class StringType extends VarType<String>{
    //CONSTRUCTOR
    public StringType(){
        super(String.class,1);
    }


    //METHODS
    public byte@NotNull[] serializeSync(@NotNull String str){
        return addVersionToBytes(str.getBytes(StandardCharsets.UTF_8));
    }
    public@NotNull String deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull String deserialize(int version,byte[]bytes){
        if(version==1)return new String(bytes,StandardCharsets.UTF_8);
        else throw createUnsupportedVersionException(version);
    }
}