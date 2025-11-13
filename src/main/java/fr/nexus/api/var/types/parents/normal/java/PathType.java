package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class PathType extends VarType<Path>{
    //CONSTRUCTOR
    public PathType(){
        super(Path.class,1);
    }


    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Path value){
        return VarTypes.STRING.serializeSync(value.toAbsolutePath().toString());
    }
    public@NotNull Path deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull Path deserialize(int version,byte[]bytes){
        if(version==1)return Path.of(VarTypes.STRING.deserializeSync(bytes));
        else throw createUnsupportedVersionException(version);
    }
}