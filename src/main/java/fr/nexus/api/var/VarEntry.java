package fr.nexus.api.var;

import fr.nexus.api.var.types.parents.Vars;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public record VarEntry<V>(@NotNull V value, @NotNull Vars type, boolean persistent){}