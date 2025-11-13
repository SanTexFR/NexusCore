//package fr.nexus.api.var.varObjects.sql;
//
//import fr.nexus.api.var.VarSql;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.UUID;
//import java.util.concurrent.CompletableFuture;
//
//@SuppressWarnings({"unused","UnusedReturnValue"})
//public class TestSql extends VarObjectSql<UUID>{
//    //CONSTRUCTOR
//    private TestSql(@NotNull Class<TestSql>clazz, @NotNull UUID uuid, @NotNull String database, @NotNull String tableName, @NotNull String path, @NotNull VarSql var){
//        super(clazz,uuid,database,tableName,path,var);
//    }
//
//    //METHODS (STATICS)
//    public static@NotNull TestSql getSync(@NotNull UUID uuid){
//        return getVarObjectSync(TestSql.class,uuid, TestSql::new,"main","playerdata",uuid.toString(),null);
//    }
//    public static@NotNull CompletableFuture<@NotNull TestSql>getAsync(@NotNull UUID uuid){
//        return getVarObjectAsync(TestSql.class,uuid, TestSql::new,"main","playerdata",uuid.toString(),null);
//    }
//}