package fr.nexus.api.var.varObjects.sql;

import fr.nexus.api.var.Var;
import fr.nexus.api.var.VarSql;
import fr.nexus.api.var.varObjects.VarObjectBackend;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@SuppressWarnings({"unused","UnusedReturnValue","unchecked"})
public abstract class VarObjectSql<R> extends VarObjectBackend<R> {

    // CONSTRUCTOR
    protected <T extends VarObjectSql<R>> VarObjectSql(@NotNull Class<T> clazz, @NotNull R reference, @NotNull String db, @NotNull String table, @NotNull String path, @NotNull VarSql var) {
        super(clazz, reference, getKey("sql", clazz.getName(), db, table, path), var);
    }

    // --- NOUVELLES MÉTHODES BASÉES SUR L'ANNOTATION @VarSqlEntity ---

    public static <R, T extends VarObjectSql<R>> @NotNull CompletableFuture<T> getAsync(@NotNull Class<T> clazz, @NotNull R reference, @NotNull VarObjectSqlFactory<R, T> factory) {
        return getAsync(clazz, reference, factory, null, null);
    }

    public static <R, T extends VarObjectSql<R>> @NotNull CompletableFuture<T> getAsync(
            @NotNull Class<T> clazz,
            @NotNull R reference,
            @NotNull VarObjectSqlFactory<R, T> factory,
            @Nullable Consumer<@NotNull Var> notCachedConsumer,
            @Nullable Runnable unloadRunnable) {

        final VarSqlEntity entity = clazz.getAnnotation(VarSqlEntity.class);
        if (entity == null) {
            throw new IllegalArgumentException("La classe " + clazz.getName() + " n'est pas annotée avec @VarSqlEntity");
        }

        final SqlKeyType<Object> keyType = (SqlKeyType<Object>) entity.keyType().getSqlKeyType();

        return getVarObjectAsync(clazz, reference, factory, entity.db(), entity.table(), keyType, reference, notCachedConsumer, unloadRunnable);
    }

    public static <R, T extends VarObjectSql<R>> @NotNull T getSync(@NotNull Class<T> clazz, @NotNull R reference, @NotNull VarObjectSqlFactory<R, T> factory) {
        return getAsync(clazz, reference, factory).join();
    }

    public static <T extends VarObjectSql<?>> boolean isLoaded(@NotNull Class<T> clazz, @NotNull Object reference) {
        final VarSqlEntity entity = clazz.getAnnotation(VarSqlEntity.class);
        if (entity == null) return false;
        return isLoaded(clazz, entity.db(), entity.table(), String.valueOf(reference));
    }

    public static <R, K, T extends VarObjectSql<R>> @NotNull T getVarObjectSync(@NotNull Class<T> clazz, @NotNull R reference, @NotNull VarObjectSqlFactory<R, T> factory, @NotNull String db, @NotNull String table, @NotNull SqlKeyType<K> keyType, @NotNull K pathKey, @Nullable Consumer<@NotNull Var> notCachedConsumer, @Nullable Runnable unloadRunnable) {
        return getVarObjectAsync(clazz, reference, factory, db, table, keyType, pathKey, notCachedConsumer, unloadRunnable).join();
    }

    public static <R, K, T extends VarObjectSql<R>> @NotNull CompletableFuture<T> getVarObjectAsync(@NotNull Class<T> clazz, @NotNull R reference, @NotNull VarObjectSqlFactory<R, T> factory, @NotNull String db, @NotNull String table, @NotNull SqlKeyType<K> keyType, @NotNull K pathKey, @Nullable Consumer<@NotNull Var> notCachedConsumer, @Nullable Runnable unloadRunnable) {
        return getVarObjectAsyncInner("sql", clazz, () ->
                        VarSql.getVarAsync(db, table, keyType, pathKey, notCachedConsumer, unloadRunnable)
                                .thenApply(var -> factory.create(clazz, reference, db, table, pathKey.toString(), var))
                , db, table, pathKey.toString());
    }

    public static <T extends VarObjectSql<?>> boolean isLoaded(@NotNull Class<T> clazz, @NotNull String db, @NotNull String table, @NotNull String path) {
        String fullPath = getKey("sql", clazz.getName(), db, table, path);
        return VarObjectBackend.isLoaded(fullPath, clazz);
    }
}