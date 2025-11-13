package fr.nexus.api.lang;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class PlaceholderContext{
    //VARIABLES (INSTANCES)
    final@NotNull Map<@NotNull String,@NotNull Component>components=new HashMap<>();

    //METHODS (INSTANCES)
    public@NotNull PlaceholderContext append(@NotNull String key,@NotNull Component value){
        components.put(key,value);
        return this;
    }
    public@NotNull PlaceholderContext append(@NotNull String key,@NotNull Object value){
        append(key,Component.text(value.toString()));
        return this;
    }

    public@NotNull PlaceholderContext copy(){
        final PlaceholderContext copy=new PlaceholderContext();
        copy.components.putAll(this.components);
        return copy;
    }
}