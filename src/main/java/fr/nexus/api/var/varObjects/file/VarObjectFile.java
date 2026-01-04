package fr.nexus.api.var.varObjects.file;

import fr.nexus.api.var.Var;
import fr.nexus.api.var.varObjects.VarObjectBackend;
import fr.nexus.api.var.VarFile;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@SuppressWarnings({"unused","UnusedReturnValue"})
public abstract class VarObjectFile<R>extends VarObjectBackend<R> {
    //CONSTRUCTOR
    protected<T extends VarObjectFile<R>> VarObjectFile(@NotNull Class<T>clazz, @NotNull R reference, @NotNull Plugin plugin, @NotNull String varPath, @NotNull VarFile var){
        super(clazz,reference,getKey("file",clazz.getName(),plugin.getName(),varPath),var);
    }

    //METHODS (STATICS)
    public static<R,T extends VarObjectFile<R>>@NotNull T getVarObjectSync(@NotNull Class<T>clazz, @NotNull R reference, @NotNull VarObjectFileFactory<R,T> factory, @NotNull Plugin plugin, @NotNull String varPath,@Nullable Consumer<@NotNull Var>notCachedConsumer,@Nullable Runnable unloadRunnable){
        return getVarObjectAsync(clazz,reference,factory,plugin,varPath,notCachedConsumer,unloadRunnable).join();
    }
    public static<R,T extends VarObjectFile<R>>@NotNull CompletableFuture<T> getVarObjectAsync(@NotNull Class<T>clazz, @NotNull R reference, @NotNull VarObjectFileFactory<R,T> factory, @NotNull Plugin plugin, @NotNull String varPath,@Nullable Consumer<@NotNull Var>notCachedConsumer,@Nullable Runnable unloadRunnable){
        return getVarObjectAsyncInner("file",clazz,()->
                VarFile.getVarAsync(plugin,varPath,unloadRunnable,notCachedConsumer).thenApply(var->factory.create(clazz,reference,plugin,varPath,var))
                ,clazz.getName(),plugin.getName(),varPath);
    }
}