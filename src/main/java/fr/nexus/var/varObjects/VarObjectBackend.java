package fr.nexus.var.varObjects;

import fr.nexus.Core;
import fr.nexus.listeners.core.CoreReloadEvent;
import fr.nexus.listeners.Listeners;
import fr.nexus.listeners.server.ServerStartEvent;
import fr.nexus.listeners.server.ServerStopEvent;
import fr.nexus.logger.Logger;
import fr.nexus.var.Var;
import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings({"unused","UnusedReturnValue","unchecked"})
public abstract class VarObjectBackend<R>{
    //VARIABLES (STATICS)
    private static final@NotNull Logger logger=new Logger(Core.getInstance(), VarObjectBackend.class);
    protected static final@NotNull ConcurrentHashMap<@NotNull String,@NotNull CompletableFuture<@NotNull VarObjectBackend<?>>>asyncLoads=new ConcurrentHashMap<>();
    protected static final@NotNull ConcurrentHashMap<@NotNull String,@NotNull WeakReference<@NotNull VarObjectBackend<?>>>varObjects=new ConcurrentHashMap<>();

    private static BukkitTask saveTask;
    static{
        Listeners.register(ServerStartEvent.class, VarObjectBackend::onServerStart,EventPriority.LOWEST);
        Listeners.register(ServerStopEvent.class, VarObjectBackend::onServerStop,EventPriority.LOWEST);
    }

    //VARIABLES (INSTANCES)
    private final@NotNull R key;
    private final@NotNull Var var;

    private final@NotNull AtomicBoolean atomicBool=new AtomicBoolean();
    private final@NotNull Cleaner.Cleanable cleanable;

    //CONSTRUCTOR
    protected<T extends VarObjectBackend<R>> VarObjectBackend(@NotNull Class<T>clazz, @NotNull R reference, @NotNull String completePath, @NotNull Var var){
        //KEY
        this.key=reference;

        //VAR
        this.var=var;

        //CLEANER
        cleanable=Core.getCleaner().register(this,new Unload(var,completePath,this.atomicBool));
    }


    //METHODS (STATICS)

    //GET
    protected static<R,T extends VarObjectBackend<R>>@NotNull T getVarObjectSyncInner(@NotNull String keyPrefix, @NotNull Class<T> clazz,@NotNull Supplier<T>factory, @NotNull Object...keyArgs){
        final String completePath=getKey(keyPrefix,clazz.getName(),stringify(keyArgs));
        final T cached=getIfCached(completePath,clazz);
        if(cached!=null)return cached;

        final CompletableFuture<VarObjectBackend<?>>async=asyncLoads.get(completePath);
        if(async!=null)return(T)async.join();

        final T varObject=factory.get();
        varObjects.put(completePath,new WeakReference<>(varObject));
        return varObject;
    }
    protected static<R,T extends VarObjectBackend<R>>@NotNull CompletableFuture<T>getVarObjectAsyncInner(@NotNull String keyPrefix,@NotNull Class<T> clazz,@NotNull Supplier<CompletableFuture<T>>factory,@NotNull Object...keyArgs){
        final String completePath=getKey(keyPrefix,clazz.getName(),stringify(keyArgs));
        final T cached=getIfCached(completePath,clazz);
        if(cached!=null)return CompletableFuture.completedFuture(cached);

        final CompletableFuture<VarObjectBackend<?>> existing = asyncLoads.get(completePath);
        if(existing!=null)return existing.thenApply(varObject->(T)varObject);

        final CompletableFuture<T>future=factory.get();

        asyncLoads.put(completePath,future.thenApply(mesh->mesh));
        future.thenAccept(res->varObjects.put(completePath,new WeakReference<>(res)));
        return future;
    }
    private static<T extends VarObjectBackend<?>>T getIfCached(@NotNull String completePath,@NotNull Class<T>clazz){
        final WeakReference<?>weak=varObjects.get(completePath);
        if(weak==null)return null;
        final Object mesh=weak.get();
        if(mesh==null)return null;
        if(clazz.isInstance(mesh))return clazz.cast(mesh);
        return null;
    }
    private static String stringify(@NotNull Object...args){
        return String.join("/",Arrays.stream(args).map(String::valueOf).toArray(String[]::new));
    }

    /**
     * Retourne tous les Meshs actuellement chargés (non null).
     *
     * @return un Set des Meshs chargés
     */
    public static@NotNull Set<VarObjectBackend<?>>getLoadedVarObjects(){
        return varObjects.values().stream()
                .map(WeakReference::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
    /**
     * Construit une clé unique à partir de plusieurs segments.
     * Exemple : getKey("player", uuid.toString()) -> "player/uuid".
     *
     * @param args segments de la clé
     * @return clé unique sous forme de String
     */
    protected static@NotNull String getKey(@NotNull String...args){
        return String.join("/",args);
    }
    /**
     * Nettoie la map des Meshs en supprimant ceux dont la référence faible est libérée.
     */
    private static void cleanVarObjectMap(){
        varObjects.entrySet().removeIf(entry->entry.getValue().get()==null);
    }

    //METHODS (INSTANCES)

    //KEY
    /** @return la clé unique du Mesh */
    public@NotNull R getKey(){
        return this.key;
    }

    //VAR
    /** @return la {@link Var} associée au Mesh */
    public@NotNull Var getVar(){
        return this.var;
    }

    //LISTENERS
    private static void onCoreReload(CoreReloadEvent e){
        if(saveTask!=null)saveTask.cancel();

        onServerStart(null);
    }
    private static void onServerStart(ServerStartEvent e){
        //PERIODICAL SAVE
        saveTask=Bukkit.getScheduler().runTaskTimer(Core.getInstance(),()->{
            final long startMillis=System.currentTimeMillis();

            cleanVarObjectMap();

            CompletableFuture.allOf(
                    varObjects.values().stream()
                            .map(WeakReference::get)
                            .filter(Objects::nonNull)
                            .map(varObject -> varObject.getVar().saveAsync())
                            .toArray(CompletableFuture[]::new)
            ).thenRun(()->
                    logger.info("✅ Mesh saves "+(System.currentTimeMillis()-startMillis)+" ms !")
            ).exceptionally(ex->{
                logger.severe("❌ Mesh saves error: "+ex.getMessage());
                return null;
            });
        },Core.CLEANUP_INTERVAL,Core.CLEANUP_INTERVAL);
    }
    private static void onServerStop(ServerStopEvent e){
        //SAVE TASK
        if(saveTask!=null)saveTask.cancel();

        //SAVE
        varObjects.values().forEach(weakMesh->{
            final VarObjectBackend<?> varObject=weakMesh.get();
            if(varObject==null)return;

            varObject.getVar().saveSync();
            varObject.atomicBool.set(true);
            varObject.cleanable.clean();
        });
    }

    //INNER CLASS
    private record Unload(@NotNull Var var,@NotNull String path,@NotNull AtomicBoolean atomicBool)implements Runnable{
        @Override
        public void run(){
            if(!atomicBool.get())var.saveSync();

            var.unload();
            varObjects.remove(path);
        }
    }
}