package fr.nexus.api.var.varObjects.sql;

import fr.nexus.api.var.Var;
import fr.nexus.api.var.VarSql;
import fr.nexus.api.var.varObjects.VarObjectBackend;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public abstract class VarObjectSql<R> extends VarObjectBackend<R> {

    // CONSTRUCTOR (Idée 6: Déduction automatique depuis l'annotation)
    protected <T extends VarObjectSql<R>> VarObjectSql(@NotNull Class<T> clazz, @NotNull R reference, @NotNull VarSql var) {
        super(clazz, reference, resolvePath(clazz, reference), var);
    }

    private static <R, T extends VarObjectSql<R>> String resolvePath(Class<T> clazz, R reference) {
        VarSqlEntity entity = clazz.getAnnotation(VarSqlEntity.class);
        if (entity == null) {
            throw new IllegalArgumentException("La classe " + clazz.getName() + " n'est pas annotée avec @VarSqlEntity");
        }
        return getKey("sql", clazz.getName(), entity.db(), entity.table(), String.valueOf(reference));
    }

    // --- API ANNOTÉE ---

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

        return getVarObjectAsyncInner("sql", clazz, () ->
                        VarSql.getVarAsync(entity.db(), entity.table(), entity.keyType(), reference, notCachedConsumer, unloadRunnable)
                                .thenApply(var -> factory.create(clazz, reference, var))
                , entity.db(), entity.table(), String.valueOf(reference));
    }

    public static <R, T extends VarObjectSql<R>> @NotNull T getSync(@NotNull Class<T> clazz, @NotNull R reference, @NotNull VarObjectSqlFactory<R, T> factory) {
        return getAsync(clazz, reference, factory).join();
    }

    // Idée 3: isLoaded universel et automatique sans redéfinition
    public static <T extends VarObjectSql<?>> boolean isLoaded(@NotNull Class<T> clazz, @NotNull Object reference) {
        final VarSqlEntity entity = clazz.getAnnotation(VarSqlEntity.class);
        if (entity == null) return false;
        String fullPath = getKey("sql", clazz.getName(), entity.db(), entity.table(), String.valueOf(reference));
        return VarObjectBackend.isLoaded(fullPath, clazz);
    }
}