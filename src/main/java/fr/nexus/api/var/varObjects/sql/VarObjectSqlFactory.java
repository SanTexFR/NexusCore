package fr.nexus.api.var.varObjects.sql;

import fr.nexus.api.var.VarSql;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
@SuppressWarnings({"unused","UnusedReturnValue"})
public interface VarObjectSqlFactory<R,T extends VarObjectSql<R>>{
    @NotNull T create(@NotNull Class<T>clazz,@NotNull R reference,@NotNull String database,@NotNull String tableName,@NotNull String path,@NotNull VarSql var);
}