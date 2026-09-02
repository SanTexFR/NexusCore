package fr.nexus.api.var.varObjects;

import com.cjcrafter.foliascheduler.TaskImplementation;
import fr.nexus.Core;
import fr.nexus.api.listeners.core.CoreCleanupEvent;
import fr.nexus.api.listeners.Listeners;
import fr.nexus.api.listeners.server.ServerStopEvent;
import fr.nexus.system.Logger;
import fr.nexus.api.var.Var;
import fr.nexus.api.var.VarSql;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings({"unused","UnusedReturnValue","unchecked"})
public abstract class VarObjectBackend<R> {
    //VARIABLES (STATICS)
    private static final @NotNull Logger logger = new Logger(Core.getInstance(), VarObjectBackend.class);
    protected static final @NotNull ConcurrentHashMap<@NotNull String, @NotNull CompletableFuture<@NotNull VarObjectBackend<?>>> asyncLoads = new ConcurrentHashMap<>();
    public static final @NotNull ConcurrentHashMap<@NotNull String, @NotNull WeakReference<@NotNull VarObjectBackend<?>>> varObjects = new ConcurrentHashMap<>();

    // Garde une référence forte vers les Backends qui doivent rester en mémoire !
    public static final @NotNull Set<VarObjectBackend<?>> shouldStayLoadedBackends = ConcurrentHashMap.newKeySet();

    private static TaskImplementation<?> saveTask;
    static {
        Listeners.register(CoreCleanupEvent.class, VarObjectBackend::onCoreCleanup);
        Listeners.register(ServerStopEvent.class, VarObjectBackend::onServerStop, EventPriority.HIGHEST);

        Core.getServerImplementation().async().runAtFixedRate(() -> {
            saveAllVarObjectsAsync();
        }, 300L, 300L, java.util.concurrent.TimeUnit.SECONDS);
    }

    //VARIABLES (INSTANCES)
    private final @NotNull R key;
    private final @NotNull Var var;

    private final @NotNull AtomicBoolean atomicBool = new AtomicBoolean();
    private final @NotNull Cleaner.Cleanable cleanable;

    //CONSTRUCTOR
    protected <T extends VarObjectBackend<R>> VarObjectBackend(@NotNull Class<T> clazz, @NotNull R reference, @NotNull String completePath, @NotNull Var var) {
        this.key = reference;
        this.var = var;

        final Unload unload = new Unload(var, completePath, this.atomicBool);
        cleanable = Core.getCleaner().register(this, unload);

        final WeakReference<VarObjectBackend<R>> weakSelf = new WeakReference<>(this);

        this.var.setOnShouldStayLoadedChanged(stayLoaded -> {
            VarObjectBackend<R> self = weakSelf.get();
            if (self != null) {
                if (stayLoaded) shouldStayLoadedBackends.add(self);
                else shouldStayLoadedBackends.remove(self);
            }
        });
    }

    // METHODS BATCH
    public static @NotNull CompletableFuture<Void> saveAllVarObjectsAsync() {
        cleanVarObjectMap();

        Set<VarSql> sqlVarsToBatch = new HashSet<>();
        List<CompletableFuture<Void>> otherSaves = new ArrayList<>();

        // Tri intelligent : SQL vers le batch, le reste en standard
        for (WeakReference<VarObjectBackend<?>> ref : varObjects.values()) {
            VarObjectBackend<?> backend = ref.get();
            if (backend == null) continue;

            Var var = backend.getVar();
            if (var instanceof VarSql varSql) {
                sqlVarsToBatch.add(varSql);
            } else {
                otherSaves.add(var.saveAsync());
            }
        }

        CompletableFuture<Void> sqlBatchFuture = VarSql.saveAllSqlVarsAsync(sqlVarsToBatch);
        CompletableFuture<Void> othersFuture = CompletableFuture.allOf(otherSaves.toArray(new CompletableFuture[0]));

        return CompletableFuture.allOf(sqlBatchFuture, othersFuture).exceptionally(ex -> {
            logger.severe("❌ Auto-save error: " + ex.getMessage());
            return null;
        });
    }

    //LOAD / GET / OTHERS
    protected static <T extends VarObjectBackend<?>> boolean isLoaded(@NotNull String completePath, @NotNull Class<T> clazz) {
        return getIfCached(completePath, clazz) != null;
    }

    protected static <R, T extends VarObjectBackend<R>> @NotNull T getVarObjectSyncInner(@NotNull String keyPrefix, @NotNull Class<T> clazz, @NotNull Supplier<T> factory, @NotNull Object... keyArgs) {
        return getVarObjectAsyncInner(keyPrefix, clazz, () -> CompletableFuture.completedFuture(factory.get()), keyArgs).join();
    }

    protected static <R, T extends VarObjectBackend<R>> @NotNull CompletableFuture<T> getVarObjectAsyncInner(@NotNull String keyPrefix, @NotNull Class<T> clazz, @NotNull Supplier<CompletableFuture<T>> factory, @NotNull Object... keyArgs) {
        final String completePath = getKey(keyPrefix, clazz.getName(), stringify(keyArgs));
        final T cached = getIfCached(completePath, clazz);
        if (cached != null) return CompletableFuture.completedFuture(cached);

        final CompletableFuture<VarObjectBackend<?>> existing = asyncLoads.get(completePath);
        if (existing != null) return existing.thenApply(varObject -> (T) varObject);

        final CompletableFuture<T> future = factory.get();

        asyncLoads.put(completePath, future.thenApply(mesh -> mesh));

        future.whenComplete((res, ex) -> {
            asyncLoads.remove(completePath);
            if (ex == null && res != null)
                varObjects.put(completePath, new WeakReference<>(res));
        });

        return future;
    }

    private static <T extends VarObjectBackend<?>> T getIfCached(@NotNull String completePath, @NotNull Class<T> clazz) {
        final WeakReference<?> weak = varObjects.get(completePath);
        if (weak == null) return null;
        final Object mesh = weak.get();
        if (mesh == null) return null;
        if (clazz.isInstance(mesh)) return clazz.cast(mesh);
        return null;
    }

    private static String stringify(@NotNull Object... args) {
        return String.join("/", Arrays.stream(args).map(String::valueOf).toArray(String[]::new));
    }

    public static @NotNull Set<VarObjectBackend<?>> getLoadedVarObjects() {
        return varObjects.values().stream()
                .map(WeakReference::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    protected static @NotNull String getKey(@NotNull String... args) {
        return String.join("/", args);
    }

    public static void cleanVarObjectMap() {
        varObjects.entrySet().removeIf(entry -> entry.getValue().get() == null);
    }

    //METHODS (INSTANCES)
    public @NotNull R getKey() { return this.key; }
    public @NotNull Var getVar() { return this.var; }

    //LISTENERS
    private static void onCoreCleanup(CoreCleanupEvent e) {
        final long startMillis = System.currentTimeMillis();

        if (!shouldStayLoadedBackends.isEmpty()) {
            for (final VarObjectBackend<?> backend : new HashSet<>(shouldStayLoadedBackends)) {
                final CompletableFuture<Boolean> completable = backend.getVar().shouldStayLoaded();
                if (completable == null) {
                    shouldStayLoadedBackends.remove(backend);
                    continue;
                }

                completable.thenAccept(bool -> {
                    if (!bool) shouldStayLoadedBackends.remove(backend);
                });
            }
        }

        saveAllVarObjectsAsync().thenRun(() ->
                logger.info("✅ Mesh saves " + (System.currentTimeMillis() - startMillis) + " ms !")
        ).exceptionally(ex -> {
            logger.severe("❌ Mesh saves error: " + ex.getMessage());
            return null;
        });
    }

    private static void onServerStop(ServerStopEvent e) {
        varObjects.values().forEach(weakMesh -> {
            final VarObjectBackend<?> varObject = weakMesh.get();
            if (varObject == null) return;
            varObject.getVar().saveSync();
            varObject.atomicBool.set(true);
            varObject.cleanable.clean();
        });
    }

    //INNER CLASS
    private record Unload(@NotNull Var var, @NotNull String path, @NotNull AtomicBoolean atomicBool) implements Runnable {
        @Override
        public void run() {
            if (!atomicBool.get()) var.forceSaveAsync().thenAccept(v -> { // Appel au forceSave pour exécution rapide hors batch
                var.unload();
                varObjects.remove(path);
            });
        }
    }
}