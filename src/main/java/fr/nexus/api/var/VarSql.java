package fr.nexus.api.var;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.nexus.Core;
import fr.nexus.api.listeners.Listeners;
import fr.nexus.api.listeners.server.ServerStopEvent;
import fr.nexus.api.var.varObjects.sql.SqlKeyType;
import fr.nexus.system.Logger;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.sql.*;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class VarSql extends Var{
    //VARIABLES (STATICS)
    private static final@NotNull Set<@NotNull String>verifiedTables=ConcurrentHashMap.newKeySet();
    private static final@NotNull Logger logger=new Logger(Core.getInstance(),VarSql.class);
    private static final@NotNull Object2ObjectOpenHashMap<@NotNull String,HikariDataSource>dataSources=new Object2ObjectOpenHashMap<>();

    static{
        Listeners.register(ServerStopEvent.class,VarSql::onServerStop);
        initializeDatabases();
    }

    //VARIABLES (INSTANCES)
    private final@NotNull String database,tableName,stringPath;
    private final @NotNull SqlKeyType<Object> keyType;
    private final @NotNull Object pathKey;

    //CONSTRUCTOR
    private <K> VarSql(@NotNull Path path, @NotNull String database, @NotNull String tableName, @NotNull String stringPath, @NotNull SqlKeyType<K> keyType, @NotNull K pathKey, @NotNull Runnable cleanupRunnable, @Nullable Consumer<@NotNull Var> notCachedConsumer) {
        super(path, cleanupRunnable);
        this.database = database;
        this.tableName = tableName;
        this.stringPath = stringPath;
        this.keyType = (SqlKeyType<Object>) keyType;
        this.pathKey = pathKey;
    }


    //METHODS (STATICS)
    public static@Nullable HikariDataSource getDatabase(@NotNull String database){
        synchronized(dataSources){
            return dataSources.get(database);
        }
    }

    public static @NotNull VarSql getVarSync(@NotNull String database, @NotNull String tableName, @NotNull String path) {
        return getVarSync(database, tableName, SqlKeyType.STRING, path, null, null);
    }
    public static <K> @NotNull VarSql getVarSync(@NotNull String database, @NotNull String tableName, @NotNull SqlKeyType<K> keyType, @NotNull K pathKey, @Nullable Consumer<@NotNull Var> notCachedConsumer, @Nullable Runnable unloadRunnable) {
        return getVarAsync(database, tableName, keyType, pathKey, notCachedConsumer, unloadRunnable).join();
    }
    public static <K> @NotNull CompletableFuture<@NotNull VarSql> getVarAsync(@NotNull String database, @NotNull String tableName, @NotNull SqlKeyType<K> keyType, @NotNull K pathKey) {
        return getVarAsync(database, tableName, keyType, pathKey, null, null);
    }

    public static <K> @NotNull CompletableFuture<@NotNull VarSql> getVarAsync(@NotNull String database, @NotNull String tableName, @NotNull SqlKeyType<K> keyType, @NotNull K pathKey, @Nullable Consumer<@NotNull Var> notCachedConsumer, @Nullable Runnable unloadRunnable) {
        final HikariDataSource hikari;
        synchronized (dataSources) {
            hikari = dataSources.get(database);
        }
        if (hikari == null) throw new RuntimeException("Unknown database: " + database);

        // FIX ICI : On passe le nom de la table en minuscules pour PostgreSQL, on laisse stringPath tranquille
        final String finalTableName = tableName.toLowerCase();
        final String stringPath = pathKey.toString();
        final Path fullPath = Path.of(database, finalTableName, stringPath);
        final String key = String.join("/", "sql", fullPath.toString());

        // 1. Vérification du cache
        final VarSql cached = getIfCached(key);
        if (cached != null) return CompletableFuture.completedFuture(cached);

        // 2. Vérification si un chargement est déjà en cours
        final CompletableFuture<Var> async;
        synchronized (asyncLoads) {
            async = asyncLoads.get(key);
        }
        if (async != null) return async.thenApply(var -> (VarSql) var);

        final VarSql var = new VarSql(fullPath, database, finalTableName, stringPath, keyType, pathKey, new Unload(key, unloadRunnable), notCachedConsumer);

        // 3. Logique de chargement optimisée
        final CompletableFuture<VarSql> future = CompletableFuture.supplyAsync(() -> {
            try {
                // Vérification de la table une seule fois par exécution du serveur
                if (!verifiedTables.contains(finalTableName)) {
                    synchronized (finalTableName.intern()) {
                        if (!verifiedTables.contains(finalTableName)) {
                            checkOrCreateTable(hikari, finalTableName, keyType);
                            verifiedTables.add(finalTableName);
                        }
                    }
                }

                // Récupération des bytes (bloquant, mais sur Loom donc OK)
                return getValue(hikari, finalTableName, keyType, pathKey);
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }, VarSerializer.LOOM_EXECUTOR).thenCompose(bytes -> {
            // Désérialisation (Potentiellement async selon ton VarSerializer)
            synchronized (var.data) {
                return VarSerializer.deserializeDataAsync(bytes, var.data)
                        .thenApply(v -> var);
            }
        });

        // 4. Gestion de la map des chargements en cours
        synchronized (asyncLoads) {
            asyncLoads.put(key, future.thenApply(v -> v));
        }

        // 5. Finalisation et mise en cache
        future.whenComplete((res, ex) -> {
            synchronized (asyncLoads) {
                asyncLoads.remove(key);
            }
            if (ex == null) {
                synchronized (vars) {
                    vars.put(key, new WeakReference<>(var));
                    if (notCachedConsumer != null) notCachedConsumer.accept(var);
                }
            } else {
                logger.severe("Erreur lors du chargement SQL pour " + stringPath);
                ex.printStackTrace();
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
    public void saveSync(){
        if(!isDirty()) return;

        final HikariDataSource hikari;
        synchronized(dataSources){
            hikari = dataSources.get(this.database);
        }
        if(hikari == null) return;

        synchronized(super.data){
            try {
                // On sérialise de manière synchrone pour l'arrêt du serveur
                byte[] serializedData = VarSerializer.serializeDataSync(super.data);
                putValue(hikari, this.tableName, this.keyType, this.pathKey, serializedData);
                setDirty(false);
            } catch (Exception e) {
                logger.severe("Erreur lors de la sauvegarde synchrone de " + this.pathKey);
                e.printStackTrace();
            }
        }
    }
    public@NotNull CompletableFuture<@Nullable Void>saveAsync(){
        if(!isDirty())return CompletableFuture.completedFuture(null);

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
                            putValue(hikari, this.tableName, this.keyType, this.pathKey, serializedData);
                        }catch(SQLException e){
                            throw new CompletionException("Failed to save data to DB: "+this.tableName,e);
                        }

                        setDirty(false);
                    },VarSerializer.LOOM_EXECUTOR)
                    .exceptionally(ex -> {
                        ex.printStackTrace();

                        return null;
                    });
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
//            config.setJdbcUrl("jdbc:mysql://"+host+":"+port+"/"+database+"?useSSL=false");
            config.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + database);
            config.setDriverClassName("org.postgresql.Driver");

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
    private static void onServerStop(ServerStopEvent e) {
        logger.info("Sauvegarde finale des données SQL avant l'arrêt...");

        // On travaille sur une copie pour éviter les ConcurrentModificationException
        synchronized (vars) {
            for (WeakReference<?> weak : vars.values()) {
                Object obj = weak.get();
                if (obj instanceof VarSql varSql) {
                    if (varSql.isDirty()) {
                        try {
                            // On appelle la sauvegarde synchrone
                            varSql.saveSync();
                        } catch (Exception ex) {
                            logger.severe("Impossible de sauvegarder " + varSql.stringPath + " à l'arrêt !");
                        }
                    }
                }
            }
        }

        // Une fois que tout est sauvegardé, on ferme les pools proprement
        synchronized (dataSources) {
            for (final HikariDataSource ds : dataSources.values()) {
                if (!ds.isClosed()) {
                    ds.close();
                }
            }
        }
        logger.info("Bases de données déconnectées.");
    }

    //LOAD
    private static <K> void checkOrCreateTable(@NotNull HikariDataSource dataSource, @NotNull String tableName, @NotNull SqlKeyType<K> keyType) throws SQLException {
        try (final Connection conn = dataSource.getConnection()) {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS \"" + tableName + "\" (" +
                                "path " + keyType.getSqlDeclaration() + " PRIMARY KEY, " +
                                "value BYTEA" +
                                ")"
                );
            }

            final DatabaseMetaData meta = conn.getMetaData();
            boolean keyOk = false, valueOk = false;

            // CORRECTION ICI : On passe directement tableName sans le .toLowerCase()
            try (final ResultSet columns = meta.getColumns(null, null, tableName, null)) {
                while (columns.next()) {
                    String colName = columns.getString("COLUMN_NAME").toLowerCase();
                    String colType = columns.getString("TYPE_NAME").toUpperCase();

                    // Validation dynamique du type !
                    if (colName.equals("path") && keyType.isValidType(colType)) keyOk = true;
                    else if (colName.equals("value") && (colType.contains("BYTEA") || colType.contains("OID"))) valueOk = true;
                }
            }

            if (!keyOk || !valueOk) {
                throw new SQLException("La table SQL '" + tableName + "' existe déjà mais sa structure est invalide.");
            }
        }
    }
//    private static void checkOrCreateTable(@NotNull HikariDataSource dataSource,@NotNull String tableName)throws SQLException{
//        try(final Connection conn=dataSource.getConnection()){
//            final DatabaseMetaData meta=conn.getMetaData();
//
//            try(ResultSet tables=meta.getTables(null,null,tableName,new String[]{"TABLE"})){
//                if(!tables.next()){
//                    try(final Statement stmt=conn.createStatement()){
////                        stmt.executeUpdate(
////                                "CREATE TABLE "+tableName+" ("+
////                                        "path VARCHAR(255) PRIMARY KEY, "+
////                                        "value LONGBLOB"+
////                                        ")"
////                        );
//                        stmt.executeUpdate(
//                                "CREATE TABLE " + tableName + " (" +
//                                        "path VARCHAR(255) PRIMARY KEY, " +
//                                        "value BYTEA" +
//                                        ")"
//                        );
//                        return;
//                    }
//                }
//            }
//
//            boolean keyOk=false,valueOk=false;
//            try(final ResultSet columns=meta.getColumns(null,null,tableName,null)) {
//                while(columns.next()){
//                    final String columnName=columns.getString("COLUMN_NAME").toLowerCase();
//                    final String columnType=columns.getString("TYPE_NAME").toUpperCase();
//
//                    if(columnName.equals("path") && columnType.contains("CHAR"))keyOk=true;
////                    if(columnName.equals("value") && columnType.contains("LONGBLOB"))valueOk=true;
//                    if (columnName.equals("value") && (columnType.contains("BYTEA") || columnType.contains("OID"))) valueOk = true;
//                }
//            }
//
//            if(!keyOk||!valueOk)
//                throw new SQLException("Table '" + tableName + "' must have columns: path (VARCHAR), value (LONGBLOB)");
//        }
//    }
    private static <K> byte[] getValue(@NotNull HikariDataSource dataSource, @NotNull String tableName, @NotNull SqlKeyType<K> keyType, @NotNull K pathKey) throws SQLException {
        final String sql = "SELECT value FROM \"" + tableName + "\" WHERE path = ?";
    try (final Connection conn = dataSource.getConnection();
         final PreparedStatement stmt = conn.prepareStatement(sql)) {

        keyType.setParameter(stmt, 1, pathKey);
        try (final ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getBytes("value");
        }
    }
    return new byte[]{};
}

    private static <K> void putValue(@NotNull HikariDataSource dataSource, @NotNull String tableName, @NotNull SqlKeyType<K> keyType, @NotNull K pathKey, byte[] value) throws SQLException {
        if (value == null || value.length == 0) {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM \"" + tableName + "\" WHERE path = ?")) {
                keyType.setParameter(stmt, 1, pathKey);
                stmt.executeUpdate();
            }
        } else {
            final String sql = "INSERT INTO \"" + tableName + "\" (path, value) VALUES (?, ?) ON CONFLICT (path) DO UPDATE SET value = EXCLUDED.value";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                keyType.setParameter(stmt, 1, pathKey);
                stmt.setBytes(2, value);
                stmt.executeUpdate();
            }
        }
    }


    //INNER CLASS
    private record Unload(@NotNull String path,@Nullable Runnable unloadRunnable)implements Runnable{
        @Override
        public void run(){
            synchronized(vars){
                vars.remove(path);
            }

            if(unloadRunnable!=null)unloadRunnable.run();
        }
    }
}