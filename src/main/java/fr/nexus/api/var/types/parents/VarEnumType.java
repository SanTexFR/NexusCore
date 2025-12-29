package fr.nexus.api.var.types.parents;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public abstract class VarEnumType<T extends Enum<T>>extends VarType<T> {
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
        final int version=readVersionAndRemainder(bytes);
        return deserializeSync(version,bytes);
    }
    public @NotNull T deserializeSync(int version,byte[]bytes){
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