package fr.nexus.api.var.types.parents;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface Vars{
    //RAW TYPE
    @NotNull String getStringType();

    //TYPE
    default boolean isWrapper(){
        return false;
    }
    default boolean isMap(){
        return false;
    }

    //EQUAL
    default boolean equals(@NotNull Vars var){
        return getStringType().equals(var.getStringType());
    }
}