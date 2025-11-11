package fr.nexus.var.varObjects.sql;

import fr.nexus.var.Var;
import fr.nexus.var.varObjects.VarObjectBackend;
import fr.nexus.var.VarSql;
import it.unimi.dsi.fastutil.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"unused","UnusedReturnValue","deprecation"})
public abstract class VarObjectSql<R>extends VarObjectBackend<R>{
    //CONSTRUCTOR
    protected<T extends VarObjectSql<R>>VarObjectSql(@NotNull Class<T>clazz, @NotNull R reference, @NotNull String db, @NotNull String table, @NotNull String path, @NotNull VarSql var){
        super(clazz,reference,getKey("sql",clazz.getName(),db,table,path),var);
    }

    //METHODS (STATICS)
    public static<R,T extends VarObjectSql<R>>@NotNull T getVarObjectSync(@NotNull Class<T> clazz, @NotNull R reference, @NotNull VarObjectSqlFactory<R,T> factory, @NotNull String db, @NotNull String table, @NotNull String path,@Nullable Function<@NotNull Var,@NotNull CompletableFuture<@NotNull Boolean>> shouldStayLoaded){
        return getVarObjectSyncInner("sql",clazz,()->
                        factory.create(clazz,reference,db,table,path,VarSql.getVarSync(db,table,path,shouldStayLoaded))
                ,clazz.getName(),db,table,path);
    }
    public static<R,T extends VarObjectSql<R>>@NotNull CompletableFuture<T>getVarObjectAsync(@NotNull Class<T> clazz, @NotNull R reference, @NotNull VarObjectSqlFactory<R,T> factory, @NotNull String db, @NotNull String table, @NotNull String path, @Nullable Function<@NotNull Var,@NotNull CompletableFuture<@NotNull Boolean>> shouldStayLoaded){
        return getVarObjectAsyncInner("sql",clazz,()->
                        VarSql.getVarAsync(db,table,path,shouldStayLoaded).thenApply(var->factory.create(clazz,reference,db,table,path,var))
                ,clazz.getName(),db,table,path);
    }
}