package fr.nexus.var.varObjects.file;

import fr.nexus.var.VarFile;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
@SuppressWarnings({"unused","UnusedReturnValue"})
public interface VarObjectFileFactory<R,T extends VarObjectFile<R>>{
    @NotNull T create(@NotNull Class<T>clazz,@NotNull R reference,@NotNull Plugin plugin,@NotNull String varPath,@NotNull VarFile var);
}