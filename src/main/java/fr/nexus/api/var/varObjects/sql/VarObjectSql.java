package fr.nexus.api.var.varObjects.sql;

import fr.nexus.api.var.Var;
import fr.nexus.api.var.varObjects.VarObjectBackend;
import fr.nexus.api.var.VarSql;
import fr.nexus.api.var.varObjects.file.VarObjectFile;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@SuppressWarnings({"unused","UnusedReturnValue"})
public abstract class VarObjectSql<R>extends VarObjectBackend<R>{
    //CONSTRUCTOR
    protected<T extends VarObjectSql<R>>VarObjectSql(@NotNull Class<T>clazz, @NotNull R reference, @NotNull String db, @NotNull String table, @NotNull String path, @NotNull VarSql var){
        super(clazz,reference,getKey("sql",clazz.getName(),db,table,path),var);
    }

    //METHODS (STATICS)
    public static<R,T extends VarObjectSql<R>>@NotNull T getVarObjectSync(@NotNull Class<T> clazz, @NotNull R reference, @NotNull VarObjectSqlFactory<R,T> factory, @NotNull String db, @NotNull String table, @NotNull String path,@Nullable Consumer<@NotNull Var>notCachedConsumer,@Nullable Runnable unloadRunnable){
        return getVarObjectAsync(clazz,reference,factory,db,table,path,notCachedConsumer,unloadRunnable).join();
    }
    public static<R,T extends VarObjectSql<R>>@NotNull CompletableFuture<T> getVarObjectAsync(@NotNull Class<T> clazz, @NotNull R reference, @NotNull VarObjectSqlFactory<R,T> factory, @NotNull String db, @NotNull String table, @NotNull String path,@Nullable Consumer<@NotNull Var>notCachedConsumer,@Nullable Runnable unloadRunnable){
        return getVarObjectAsyncInner("sql", clazz, () ->
                        VarSql.getVarAsync(db, table, path, notCachedConsumer, unloadRunnable)
                                .thenApply(var -> factory.create(clazz, reference, db, table, path, var))
                , db, table, path);
    }

    public static <T extends VarObjectSql<?>> boolean isLoaded(@NotNull Class<T> clazz, @NotNull String db, @NotNull String table, @NotNull String path) {
        String fullPath = getKey("sql", clazz.getName(), db, table, path);
        return VarObjectBackend.isLoaded(fullPath, clazz);
    }
}