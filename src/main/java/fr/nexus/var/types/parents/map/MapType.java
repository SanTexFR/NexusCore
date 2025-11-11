package fr.nexus.var.types.parents.map;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings({"unused","UnusedReturnValue"})
public abstract class MapType<T extends Map<?,?>>{
    //VARIABLES (STATICS)
    private static final@NotNull Map<@NotNull String,@NotNull MapType<?>>types=new HashMap<>();

    //VARIABLES (INSTANCES)
    private final@NotNull Class<@NotNull T>typeClazz;
    private final@NotNull Supplier<@NotNull T>supplier;

    //CONSTRUCTOR
    protected MapType(@NotNull Class<@NotNull T>typeClazz,@NotNull Supplier<@NotNull T>supplier){
        this.typeClazz=typeClazz;
        this.supplier=supplier;
        types.put(getStringType(),this);
    }


    //METHODS (STATICS)
    public static@NotNull Map<@NotNull String,@NotNull MapType<?>>getTypes(){
        return types;
    }

    //METHODS (INSTANCES)

    //RAW TYPE
    public@NotNull String getStringType(){
        return"Map<"+this.typeClazz.getName()+">";
    }
    public@NotNull Supplier<@NotNull T>getSupplier(){
        return this.supplier;
    }
}
