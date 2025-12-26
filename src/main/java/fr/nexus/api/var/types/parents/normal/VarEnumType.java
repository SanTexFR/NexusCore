package fr.nexus.api.var.types.parents.normal;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.VarVersion;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public abstract class VarEnumType<T extends Enum<T>>extends VarType<T>{
    //CONSTRUCTOR
    protected VarEnumType(@NotNull Class<@NotNull T>typeClazz){
        super(typeClazz,1);
    }

    //METHODS(INSTANCES)
    @Override
    public byte@NotNull[]serializeSync(@NotNull T type){
        return addVersionToBytes(VarTypes.STRING.serializeSync(type.name()));
    }

    @Override
    public @NotNull T deserializeSync(byte@NotNull[]bytes){
        final VarVersion.VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private @NotNull T deserialize(int version,byte[]bytes){
        if(version!=1)throw createUnsupportedVersionException(version);

        final String str=VarTypes.STRING.deserializeSync(bytes);
        final T type;
        try{
            type=Enum.valueOf(getTypeClazz(),str);
        }catch(IllegalArgumentException e){
            throw new RuntimeException(e);
        }

        return type;
    }
}