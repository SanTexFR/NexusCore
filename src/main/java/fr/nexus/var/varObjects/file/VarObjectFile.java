package fr.nexus.var.varObjects.file;

import fr.nexus.var.Var;
import fr.nexus.var.varObjects.VarObjectBackend;
import fr.nexus.var.VarFile;
import it.unimi.dsi.fastutil.Function;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"unused","UnusedReturnValue","deprecation"})
public abstract class VarObjectFile<R>extends VarObjectBackend<R> {
    //CONSTRUCTOR
    protected<T extends VarObjectFile<R>> VarObjectFile(@NotNull Class<T>clazz, @NotNull R reference, @NotNull Plugin plugin, @NotNull String varPath, @NotNull VarFile var){
        super(clazz,reference,getKey("file",clazz.getName(),plugin.getName(),varPath),var);
    }

    //METHODS (STATICS)
    public static<R,T extends VarObjectFile<R>>@NotNull T getVarObjectSync(@NotNull Class<T>clazz, @NotNull R reference, @NotNull VarObjectFileFactory<R,T> factory, @NotNull Plugin plugin, @NotNull String varPath,@Nullable Function<@NotNull Var,@NotNull CompletableFuture<@NotNull Boolean>> shouldStayLoaded){
        return getVarObjectSyncInner("file",clazz,()->
                factory.create(clazz,reference,plugin,varPath,VarFile.getVarSync(plugin,varPath,shouldStayLoaded))
                ,clazz.getName(),plugin.getName(),varPath);
    }
    public static<R,T extends VarObjectFile<R>>@NotNull CompletableFuture<T> getVarObjectAsync(@NotNull Class<T>clazz, @NotNull R reference, @NotNull VarObjectFileFactory<R,T> factory, @NotNull Plugin plugin, @NotNull String varPath, @Nullable Function<@NotNull Var,@NotNull CompletableFuture<@NotNull Boolean>> shouldStayLoaded){
        return getVarObjectAsyncInner("file",clazz,()->
                VarFile.getVarAsync(plugin,varPath,shouldStayLoaded).thenApply(var->factory.create(clazz,reference,plugin,varPath,var))
                ,clazz.getName(),plugin.getName(),varPath);
    }
}