package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class PathType extends VarType<Path>{
    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Path value){
        return VarTypes.STRING.serializeSync(value.toAbsolutePath().toString());
    }

    public@NotNull Path deserializeSync(int version,byte[]bytes){
        if(version==1)return Path.of(VarTypes.STRING.deserializeSync(bytes));
        else throw createUnsupportedVersionException(version);
    }
}