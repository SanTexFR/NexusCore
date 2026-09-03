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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "UnusedReturnValue", "SqlResolve", "SqlNoDataSourceInspection"})
public class VarSql extends Var {

    private static final @NotNull Set<@NotNull String> verifiedTables = ConcurrentHashMap.newKeySet();
    private static final @NotNull Logger logger = new Logger(Core.getInstance(), VarSql.class);
    private static final @NotNull Object2ObjectOpenHashMap<@NotNull String, HikariDataSource> dataSources = new Object2ObjectOpenHashMap<>();

    static {
        Listeners.register(ServerStopEvent.class, VarSql::onServerStop);
        initializeDatabases();
    }

    private final @NotNull String database, tableName, stringPath;
    private final @NotNull SqlKeyType keyType;
    private final @NotNull Object pathKey;

    private <K> VarSql(@NotNull Path path, @NotNull String database, @NotNull String tableName, @NotNull String stringPath, @NotNull SqlKeyType keyType, @NotNull K pathKey, @NotNull Runnable cleanupRunnable, @Nullable Consumer<@NotNull Var> notCachedConsumer) {
        super(path, cleanupRunnable);
        this.database = database;
        this.tableName = tableName;
        this.stringPath = stringPath;
        this.keyType = keyType;
        this.pathKey = pathKey;
    }

    public static @Nullable HikariDataSource getDatabase(@NotNull String database) {
        synchronized(dataSources) {
            return dataSources.get(database);
        }
    }

    public static @NotNull VarSql getVarSync(@NotNull String database, @NotNull String tableName, @NotNull String path) {
        return getVarSync(database, tableName, SqlKeyType.STRING, path, null, null);
    }

    public static <K> @NotNull VarSql getVarSync(@NotNull String database, @NotNull String tableName, @NotNull SqlKeyType keyType, @NotNull K pathKey, @Nullable Consumer<@NotNull Var> notCachedConsumer, @Nullable Runnable unloadRunnable) {
        return getVarAsync(database, tableName, keyType, pathKey, notCachedConsumer, unloadRunnable).join();
    }

    public static <K> @NotNull CompletableFuture<@NotNull VarSql> getVarAsync(@NotNull String database, @NotNull String tableName, @NotNull SqlKeyType keyType, @NotNull K pathKey) {
        return getVarAsync(database, tableName, keyType, pathKey, null, null);
    }

    public static <K> @NotNull CompletableFuture<@NotNull VarSql> getVarAsync(@NotNull String database, @NotNull String tableName, @NotNull SqlKeyType keyType, @NotNull K pathKey, @Nullable Consumer<@NotNull Var> notCachedConsumer, @Nullable Runnable unloadRunnable) {
        final HikariDataSource hikari;
        synchronized (dataSources) {
            hikari = dataSources.get(database);
        }
        if (hikari == null) throw new RuntimeException("Unknown database: " + database);

        final String finalTableName = tableName.toLowerCase();
        final String stringPath = pathKey.toString();
        final Path fullPath = Path.of(database, finalTableName, stringPath);
        final String key = String.join("/", "sql", fullPath.toString());

        final VarSql cached = getIfCached(key);
        if (cached != null) return CompletableFuture.completedFuture(cached);

        final CompletableFuture<Var> async;
        synchronized (asyncLoads) {
            async = asyncLoads.get(key);
        }
        if (async != null) return async.thenApply(var -> (VarSql) var);

        final VarSql var = new VarSql(fullPath, database, finalTableName, stringPath, keyType, pathKey, new Unload(key, unloadRunnable), notCachedConsumer);

        final CompletableFuture<VarSql> future = CompletableFuture.supplyAsync(() -> {
            try {
                if (!verifiedTables.contains(finalTableName)) {
                    synchronized (finalTableName.intern()) {
                        if (!verifiedTables.contains(finalTableName)) {
                            checkOrCreateTable(hikari, finalTableName, keyType);
                            verifiedTables.add(finalTableName);
                        }
                    }
                }
                return getValue(hikari, finalTableName, keyType, pathKey);
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        }, VarSerializer.LOOM_EXECUTOR).thenCompose(bytes -> {
            synchronized (var.data) {
                return VarSerializer.deserializeDataAsync(bytes, var.data).thenApply(v -> var);
            }
        });

        synchronized (asyncLoads) {
            asyncLoads.put(key, future.thenApply(v -> v));
        }

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

    private static @Nullable VarSql getIfCached(@NotNull String completePath) {
        final WeakReference<?> weak;
        synchronized(vars) { weak = vars.get(completePath); }
        if (weak == null) return null;
        final Object mesh = weak.get();
        if (mesh != null) return (VarSql) mesh;
        return null;
    }

    @Override
    public void saveSync() {
        if (!isDirty()) return;

        final HikariDataSource hikari;
        synchronized(dataSources) { hikari = dataSources.get(this.database); }
        if (hikari == null) return;

        synchronized(super.data) {
            try {
                byte[] serializedData = VarSerializer.serializeDataSync(super.data);
                putValue(hikari, this.tableName, this.keyType, this.pathKey, serializedData);
                setDirty(false);
            } catch (Exception e) {
                logger.severe("Erreur lors de la sauvegarde synchrone de " + this.pathKey);
                e.printStackTrace();
            }
        }
    }

    @Override
    public @NotNull CompletableFuture<@Nullable Void> saveAsync() {
        return forceSaveAsync();
    }

    @Override
    public @NotNull CompletableFuture<@Nullable Void> forceSaveAsync() {
        if (!isDirty()) return CompletableFuture.completedFuture(null);

        final HikariDataSource hikari;
        synchronized(dataSources) { hikari = dataSources.get(this.database); }
        if (hikari == null) return CompletableFuture.failedFuture(new IllegalStateException("Unknown database: " + this.database));

        synchronized(super.data) {
            return VarSerializer.serializeDataAsync(super.data)
                    .thenAcceptAsync(serializedData -> {
                        try {
                            putValue(hikari, this.tableName, this.keyType, this.pathKey, serializedData);
                        } catch(SQLException e) {
                            throw new CompletionException("Failed to save data to DB: " + this.tableName, e);
                        }
                        setDirty(false);
                    }, VarSerializer.LOOM_EXECUTOR)
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        return null;
                    });
        }
    }

    public static @NotNull CompletableFuture<Void> saveAllSqlVarsAsync(@NotNull Set<VarSql> varsToSave) {
        if (varsToSave.isEmpty()) return CompletableFuture.completedFuture(null);

        Map<String, Map<String, List<VarSql>>> grouped = new HashMap<>();
        for (VarSql var : varsToSave) {
            if (!var.isDirty()) continue;
            grouped.computeIfAbsent(var.database, k -> new HashMap<>())
                    .computeIfAbsent(var.tableName, k -> new ArrayList<>())
                    .add(var);
        }

        if (grouped.isEmpty()) return CompletableFuture.completedFuture(null);

        return CompletableFuture.runAsync(() -> {
            for (var dbEntry : grouped.entrySet()) {
                String dbName = dbEntry.getKey();
                HikariDataSource hikari;
                synchronized (dataSources) {
                    hikari = dataSources.get(dbName);
                }
                if (hikari == null) continue;

                try (Connection conn = hikari.getConnection()) {
                    conn.setAutoCommit(false);

                    for (var tableEntry : dbEntry.getValue().entrySet()) {
                        String tableName = tableEntry.getKey();
                        List<VarSql> varList = tableEntry.getValue();

                        String sql = "INSERT INTO \"" + tableName + "\" (path, value) VALUES (?, ?) " +
                                "ON CONFLICT (path) DO UPDATE SET value = EXCLUDED.value";

                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            List<VarSql> processedInBatch = new ArrayList<>();

                            for (VarSql varSql : varList) {
                                byte[] bytes;
                                synchronized (varSql.data) {
                                    bytes = VarSerializer.serializeDataSync(varSql.data);
                                }

                                if (bytes != null && bytes.length > 0) {
                                    varSql.keyType.setParameter(stmt, 1, varSql.pathKey);
                                    stmt.setBytes(2, bytes);
                                    stmt.addBatch();
                                    processedInBatch.add(varSql);
                                } else {
                                    try (PreparedStatement delStmt = conn.prepareStatement("DELETE FROM \"" + tableName + "\" WHERE path = ?")) {
                                        varSql.keyType.setParameter(delStmt, 1, varSql.pathKey);
                                        delStmt.executeUpdate();
                                    }
                                    processedInBatch.add(varSql);
                                }
                            }
                            stmt.executeBatch();

                            for (VarSql varSql : processedInBatch) varSql.setDirty(false);
                        }
                    }
                    conn.commit();
                } catch (SQLException ex) {
                    logger.severe("❌ Erreur lors du batch-save SQL : " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }, VarSerializer.LOOM_EXECUTOR);
    }

    private static void initializeDatabases() {
        ConfigurationSection dbSection = Core.getInstance().getConfig().getConfigurationSection("databases");
        if (dbSection == null) return;
        for (final String dbName : dbSection.getKeys(false)) {
            final ConfigurationSection section = dbSection.getConfigurationSection(dbName);
            if (section == null) continue;

            final String host = section.getString("host");
            final int port = section.getInt("port");
            final String username = section.getString("username");
            final String password = section.getString("password");
            final String database = section.getString("database");

            if (host == null || username == null || password == null || database == null) {
                logger.severe("Missing or wrong information(s) in database: " + dbName);
                return;
            }

            final HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + database);
            config.setDriverClassName("org.postgresql.Driver");

            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(60000);
            config.setConnectionTimeout(30000);
            config.setMaxLifetime(3_600_000);
            config.setPoolName(dbName + "-pool");

            synchronized (dataSources) {
                dataSources.put(dbName, new HikariDataSource(config));
            }
        }
    }

    private static void onServerStop(ServerStopEvent e) {
        logger.info("Sauvegarde finale des données SQL avant l'arrêt...");
        synchronized (vars) {
            for (WeakReference<?> weak : vars.values()) {
                Object obj = weak.get();
                if (obj instanceof VarSql varSql) {
                    if (varSql.isDirty()) {
                        try {
                            varSql.saveSync();
                        } catch (Exception ex) {
                            logger.severe("Impossible de sauvegarder " + varSql.stringPath + " à l'arrêt !");
                        }
                    }
                }
            }
        }
        synchronized (dataSources) {
            for (final HikariDataSource ds : dataSources.values()) {
                if (!ds.isClosed()) ds.close();
            }
        }
        logger.info("Bases de données déconnectées.");
    }

    private static void checkOrCreateTable(@NotNull HikariDataSource dataSource, @NotNull String tableName, @NotNull SqlKeyType keyType) throws SQLException {
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

            try (final ResultSet columns = meta.getColumns(null, null, tableName, null)) {
                while (columns.next()) {
                    String colName = columns.getString("COLUMN_NAME").toLowerCase();
                    String colType = columns.getString("TYPE_NAME").toUpperCase();

                    if (colName.equals("path") && keyType.isValidType(colType)) keyOk = true;
                    else if (colName.equals("value") && (colType.contains("BYTEA") || colType.contains("OID"))) valueOk = true;
                }
            }
            if (!keyOk || !valueOk) {
                throw new SQLException("La table SQL '" + tableName + "' existe déjà mais sa structure est invalide.");
            }
        }
    }

    // Idée 5: Implémentation épurée
    public static @NotNull CompletableFuture<Set<String>> getAllPathsAsync(@NotNull String database, @NotNull String tableName) {
        final HikariDataSource hikari = getDatabase(database);
        if (hikari == null) return CompletableFuture.failedFuture(new IllegalStateException("Unknown database: " + database));

        return CompletableFuture.supplyAsync(() -> {
            Set<String> keys = new HashSet<>();
            final String sql = "SELECT path FROM \"" + tableName.toLowerCase() + "\"";

            try (final Connection conn = hikari.getConnection();
                 final PreparedStatement stmt = conn.prepareStatement(sql);
                 final ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) keys.add(rs.getString(1));
            } catch (SQLException e) {
                throw new CompletionException("Erreur lors de la récupération des clés pour la table: " + tableName, e);
            }
            return keys;
        }, VarSerializer.LOOM_EXECUTOR);
    }

    private static <K> byte[] getValue(@NotNull HikariDataSource dataSource, @NotNull String tableName, @NotNull SqlKeyType keyType, @NotNull K pathKey) throws SQLException {
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

    private static <K> void putValue(@NotNull HikariDataSource dataSource, @NotNull String tableName, @NotNull SqlKeyType keyType, @NotNull K pathKey, byte[] value) throws SQLException {
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

    private record Unload(@NotNull String path, @Nullable Runnable unloadRunnable) implements Runnable {
        @Override
        public void run() {
            synchronized(vars) { vars.remove(path); }
            if (unloadRunnable != null) unloadRunnable.run();
        }
    }
}