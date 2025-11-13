package fr.nexus.var;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.nexus.Core;
import fr.nexus.performanceTracker.PerformanceTracker;
import fr.nexus.listeners.Listeners;
import fr.nexus.listeners.server.ServerStopEvent;
import fr.nexus.system.Logger;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class VarSql extends Var{
    //VARIABLES (STATICS)
    private static final@NotNull Logger logger=new Logger(Core.getInstance(),VarSql.class);
    private static final@NotNull Object2ObjectOpenHashMap<@NotNull String,HikariDataSource>dataSources=new Object2ObjectOpenHashMap<>();

    static{
        Listeners.register(ServerStopEvent.class,VarSql::onServerStop);
        initializeDatabases();
    }

    //VARIABLES (INSTANCES)
    private final@NotNull String database,tableName,dataPath;

    //CONSTRUCTOR
    private VarSql(@NotNull Path path,@NotNull String database,@NotNull String tableName,@NotNull String dataPath,@NotNull Runnable closeRunnable,@Nullable Function<@NotNull Var,@NotNull CompletableFuture<@NotNull Boolean>>shouldStayLoaded){
        super(path,closeRunnable,shouldStayLoaded);
        this.database=database;
        this.tableName=tableName;
        this.dataPath=dataPath;
    }


    //METHODS (STATICS)
    public static@Nullable HikariDataSource getDatabase(@NotNull String database){
        synchronized(dataSources){
            return dataSources.get(database);
        }
    }

    @Deprecated
    public static@NotNull VarSql getVarSync(@NotNull String database,@NotNull String tableName,@NotNull String path){
        return getVarSync(database,tableName,path,null);
    }
    public static@NotNull VarSql getVarSync(@NotNull String database,@NotNull String tableName,@NotNull String path,@Nullable Function<@NotNull Var,@NotNull CompletableFuture<@NotNull Boolean>>shouldStayLoaded){
        final long nanoTime=System.nanoTime();

        final HikariDataSource hikari;
        synchronized(dataSources){
            hikari=dataSources.get(database);
        }
        if(hikari==null){
            PerformanceTracker.increment(PerformanceTracker.Types.VAR,"getVarSync",System.nanoTime()-nanoTime);
            throw new RuntimeException("Unknown database: "+database);
        }

        final Path fullPath=Path.of(database,tableName,path);
        final String key=String.join("/","sql",fullPath.toString());

        final VarSql cached=getIfCached(key);
        if(cached!=null){
            PerformanceTracker.increment(PerformanceTracker.Types.VAR,"getVarSync",System.nanoTime()-nanoTime);
            return cached;
        }

        final CompletableFuture<Var>async;
        synchronized(asyncLoads){
            async=asyncLoads.get(key);
        }
        if(async!=null){
            PerformanceTracker.increment(PerformanceTracker.Types.VAR,"getVarSync",System.nanoTime()-nanoTime);
            return(VarSql)async.join();
        }

        final VarSql var=new VarSql(fullPath,database,tableName,path,new Unload(key),shouldStayLoaded);
        try{
            checkOrCreateTable(hikari,tableName);

            synchronized(var.data){
                VarSerializer.deserializeDataSync(getValue(hikari,tableName,path),var.data);
            }

            synchronized(vars){
                vars.put(key,new WeakReference<>(var));
            }

            PerformanceTracker.increment(PerformanceTracker.Types.VAR,"getVarSync",System.nanoTime()-nanoTime);
            return var;
        }catch(IOException|SQLException e){
            PerformanceTracker.increment(PerformanceTracker.Types.VAR,"getVarSync",System.nanoTime()-nanoTime);
            throw new RuntimeException("Failed to load sync var: "+key,e);
        }
    }
    public static@NotNull CompletableFuture<@NotNull VarSql>getVarAsync(@NotNull String database,@NotNull String tableName,@NotNull String path){
        return getVarAsync(database,tableName,path,null);
    }
    public static@NotNull CompletableFuture<@NotNull VarSql>getVarAsync(@NotNull String database,@NotNull String tableName,@NotNull String path,@Nullable Function<@NotNull Var,@NotNull CompletableFuture<@NotNull Boolean>>shouldStayLoaded){
        final HikariDataSource hikari;
        synchronized(dataSources){
            hikari=dataSources.get(database);
        }
        if(hikari==null)throw new RuntimeException("Unknown database: "+database);

        final Path fullPath=Path.of(database,tableName,path);
        final String key=String.join("/","sql",fullPath.toString());

        final VarSql cached=getIfCached(key);
        if(cached!=null)return CompletableFuture.completedFuture(cached);

        final CompletableFuture<Var>async;
        synchronized(asyncLoads){
            async=asyncLoads.get(key);
        }
        if(async!=null)return async.thenApply(var->(VarSql) var);

        final VarSql var=new VarSql(fullPath,database,tableName,path,new Unload(key),shouldStayLoaded);

        final CompletableFuture<VarSql>future=CompletableFuture
                .runAsync(()->{
                    try{
                        checkOrCreateTable(hikari,tableName);
                    }catch(SQLException e){
                        throw new CompletionException(e);
                    }
                },Var.THREADPOOL)
                .thenApplyAsync(unused->{
                    try{
                        return getValue(hikari,tableName,path);
                    }catch(SQLException e){
                        throw new CompletionException(e);
                    }
                },Var.THREADPOOL)
                .thenCompose(bytes->{
                    synchronized(var.data){
                        return VarSerializer.deserializeDataAsync(bytes,var.data)
                                .thenApply(v->var);
                    }
                });

        synchronized(asyncLoads){
            asyncLoads.put(key,future.thenApply(v->v));
        }

        future.whenComplete((res,ex)->{
            synchronized(asyncLoads){
                asyncLoads.remove(key);
            }
            if(ex==null){
                synchronized(vars){
                    vars.put(key,new WeakReference<>(var));
                }
            }
        });

        return future;
    }
    private static@Nullable VarSql getIfCached(@NotNull String completePath){
        final WeakReference<?>weak;
        synchronized(vars){
            weak=vars.get(completePath);
        }
        if(weak==null)return null;

        final Object mesh=weak.get();
        if(mesh!=null)return(VarSql)mesh;

        return null;
    }

    //ABSTRACT
    @Deprecated
    public void saveSync(){
        if(!isDirty())return;

        final long nanoTime=System.nanoTime();

        setDirty(false);

        final HikariDataSource hikari;
        synchronized(dataSources){
            hikari=dataSources.get(this.database);
        }
        if(hikari==null){
            PerformanceTracker.increment(PerformanceTracker.Types.VAR,"saveSync",System.nanoTime()-nanoTime);
            throw new IllegalStateException("Unknown database: "+this.database);
        }

        try{
            synchronized(super.data){
                putValue(hikari,this.tableName,this.dataPath,VarSerializer.serializeDataSync(super.data));
            }
        }catch(IOException|SQLException e){
            PerformanceTracker.increment(PerformanceTracker.Types.VAR,"saveSync",System.nanoTime()-nanoTime);
            throw new RuntimeException("Failed to save data synchronously to DB: "+this.tableName,e);
        }

        PerformanceTracker.increment(PerformanceTracker.Types.VAR,"saveSync",System.nanoTime()-nanoTime);
    }
    public@NotNull CompletableFuture<@Nullable Void>saveAsync(){
        if(!isDirty())return CompletableFuture.completedFuture(null);
        setDirty(false);

        final HikariDataSource hikari;
        synchronized(dataSources){
            hikari=dataSources.get(this.database);
        }
        if(hikari==null)return CompletableFuture.failedFuture(
                new IllegalStateException("Unknown database: "+this.database));

        synchronized(super.data){
            return VarSerializer.serializeDataAsync(super.data)
                    .thenAcceptAsync(serializedData ->{
                        try{
                            putValue(hikari,this.tableName,this.dataPath,serializedData);
                        }catch(SQLException e){
                            throw new CompletionException("Failed to save data to DB: "+this.tableName,e);
                        }
                    },Var.THREADPOOL);
        }
    }

    //INITIALIZER
    private static void initializeDatabases(){
        ConfigurationSection dbSection=Core.getInstance().getConfig().getConfigurationSection("databases");
        if(dbSection==null)return;
        for(final String dbName:dbSection.getKeys(false)){
            final ConfigurationSection section=dbSection.getConfigurationSection(dbName);
            if(section==null)continue;

            final String host=section.getString("host");
            final int port=section.getInt("port");
            final String username=section.getString("username");
            final String password=section.getString("password");
            final String database=section.getString("database");

            if(host==null||username==null||password==null||database==null){
                logger.severe("Missing or wrong information(s) in database: "+dbName);
                return;
            }

            final HikariConfig config=new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://"+host+":"+port+"/"+database+"?useSSL=false");
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(60000);
            config.setConnectionTimeout(30000);
            config.setMaxLifetime(3_600_000); // 1 heure en ms
            config.setPoolName(dbName+"-pool");

            synchronized(dataSources){
                dataSources.put(dbName,new HikariDataSource(config));
            }
        }
    }
    private static void onServerStop(ServerStopEvent e){
        synchronized(dataSources){
            for(final HikariDataSource ds:dataSources.values())
                ds.close();
        }
    }

    //LOAD
    private static void checkOrCreateTable(@NotNull HikariDataSource dataSource,@NotNull String tableName)throws SQLException{
        try(final Connection conn=dataSource.getConnection()){
            final DatabaseMetaData meta=conn.getMetaData();

            try(ResultSet tables=meta.getTables(null,null,tableName,new String[]{"TABLE"})){
                if(!tables.next()){
                    try(final Statement stmt=conn.createStatement()){
                        stmt.executeUpdate(
                                "CREATE TABLE "+tableName+" ("+
                                        "path VARCHAR(255) PRIMARY KEY, "+
                                        "value LONGBLOB"+
                                        ")"
                        );
                        return;
                    }
                }
            }

            boolean keyOk=false,valueOk=false;
            try(final ResultSet columns=meta.getColumns(null,null,tableName,null)) {
                while(columns.next()){
                    final String columnName=columns.getString("COLUMN_NAME").toLowerCase();
                    final String columnType=columns.getString("TYPE_NAME").toUpperCase();

                    if(columnName.equals("path") && columnType.contains("CHAR"))keyOk=true;
                    if(columnName.equals("value") && columnType.contains("LONGBLOB"))valueOk=true;
                }
            }

            if(!keyOk||!valueOk)
                throw new SQLException("Table '" + tableName + "' must have columns: path (VARCHAR), value (LONGBLOB)");
        }
    }
    private static byte[]getValue(@NotNull HikariDataSource dataSource,@NotNull String tableName,@NotNull String path)throws SQLException{
        final String sql="SELECT value FROM "+tableName+" WHERE path = ?";
        try(final Connection conn=dataSource.getConnection();
            final PreparedStatement stmt=conn.prepareStatement(sql)){

            stmt.setString(1,path);
            try(final ResultSet rs=stmt.executeQuery()){
                if(rs.next())
                    return rs.getBytes("value");
            }
        }

        return new byte[]{};
    }
    private static void putValue(@NotNull HikariDataSource dataSource,@NotNull String tableName,@NotNull String path,byte[]value)throws SQLException{
        if(value==null||value.length==0){
            final String sql="DELETE FROM "+tableName+" WHERE path = ?";
            try (final Connection conn=dataSource.getConnection();
                 final PreparedStatement stmt=conn.prepareStatement(sql)){

                stmt.setString(1,path);
                stmt.executeUpdate();

            }
        }else{
            final String sql="INSERT INTO "+tableName+" (path, value) VALUES (?, ?) ON DUPLICATE KEY UPDATE value = VALUES(value)";

            try(final Connection conn=dataSource.getConnection();
                 final PreparedStatement stmt=conn.prepareStatement(sql)){

                stmt.setString(1,path);
                stmt.setBytes(2,value);
                stmt.executeUpdate();

            }
        }
    }


    //INNER CLASS
    private record Unload(@NotNull String path)implements Runnable{
        @Override
        public void run(){
            synchronized(vars){
                vars.remove(path);
            }
        }
    }
}