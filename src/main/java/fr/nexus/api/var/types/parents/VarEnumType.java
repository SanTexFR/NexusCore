package fr.nexus.api.var.types.parents;

import fr.nexus.api.var.types.VarTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused","UnusedReturnValue"})
public abstract class VarEnumType<T extends Enum<T>>extends VarType<T>{
    //VARIABLES(INSTANCES)
    private@Nullable T defaultReturn;

    //CONSTRUCTOR
    protected VarEnumType(@NotNull Class<@NotNull T>typeClazz){
        super(typeClazz,1);
    }
    protected VarEnumType(@NotNull Class<@NotNull T>typeClazz,@Nullable T defaultReturn){
        super(typeClazz,1);
        this.defaultReturn=defaultReturn;
    }

    //METHODS(INSTANCES)
    @Override
    public byte@NotNull[]serializeSync(@NotNull T type){
        return addVersionToBytes(VarTypes.STRING.serializeSync(type.name()));
    }

    @Override
    public @NotNull T deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder value=readVersionAndRemainder(bytes);
        return deserializeSync(value.version(),value.remainder());
    }
    public @NotNull T deserializeSync(int version,byte[]bytes){
        if(version!=1)throw createUnsupportedVersionException(version);

        final String str=VarTypes.STRING.deserializeSync(bytes);
        final T type;
        try{
            type=Enum.valueOf(getTypeClazz(),str);
        }catch(IllegalArgumentException e){
            if(defaultReturn!=null)return defaultReturn;
            else throw new RuntimeException(e);
        }

        return type;
    }
}