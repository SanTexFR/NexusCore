package fr.nexus.api.var;

import fr.nexus.Core;
import fr.nexus.system.ThreadPool;
import fr.nexus.api.listeners.core.CoreDisableEvent;
import fr.nexus.api.listeners.core.CoreInitializeEvent;
import fr.nexus.api.listeners.core.CoreReloadEvent;
import fr.nexus.system.internal.performanceTracker.PerformanceTracker;
import fr.nexus.api.listeners.Listeners;
import fr.nexus.api.var.events.DataSetEvent;
import fr.nexus.api.var.events.DataSetEventType;
import fr.nexus.api.var.types.VarSubType;
import fr.nexus.api.var.types.parents.Vars;
import fr.nexus.api.var.types.parents.map.MapType;
import fr.nexus.api.var.types.parents.map.MapVarType;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"unused","UnusedReturnValue","unchecked"})
public sealed abstract class Var permits VarFile,VarSql{
    //VARIABLES (STATICS)
    public static int THREAD_AMOUNT;
    public static ThreadPool THREADPOOL;

    private static@Nullable BukkitTask cleanupTask;

    public static final@NotNull Object2ObjectOpenHashMap<@NotNull String,CompletableFuture<Var>>asyncLoads=new Object2ObjectOpenHashMap<>();
    public static final@NotNull Object2ObjectOpenHashMap<@NotNull String,WeakReference<Var>>vars=new Object2ObjectOpenHashMap<>();
    public static final@NotNull Set<@NotNull Var>shouldStayLoadedVars=new HashSet<>();
    static{
        Listeners.register(CoreInitializeEvent.class,Var::onCoreInitialize);
        Listeners.register(CoreReloadEvent.class,Var::onCoreReload);
        Listeners.register(CoreDisableEvent.class,Var::onCoreDisable);
    }

    //VARIABLES (INSTANCES)
    protected final@NotNull Object2ObjectOpenHashMap<@NotNull String,Object[]>data=new Object2ObjectOpenHashMap<>();
    private final@NotNull Path path;
    private boolean dirty;

    private final@Nullable Function<@NotNull Var,@NotNull CompletableFuture<@NotNull Boolean>>shouldStayLoaded;

    private final@NotNull Cleaner.Cleanable cleanable;

    //CONSTRUCTOR
    protected Var(@NotNull Path path,@NotNull Runnable closeRunnable,@Nullable Function<@NotNull Var,@NotNull CompletableFuture<@NotNull Boolean>>shouldStayLoaded){
        this.path=path;

        this.cleanable=Core.getCleaner().register(this,closeRunnable);

        if(shouldStayLoaded!=null){
            this.shouldStayLoaded=shouldStayLoaded;
            shouldStayLoadedVars.add(this);
        }else this.shouldStayLoaded=null;
    }

    //METHODS (STATICS)

    //LISTENERS
    private static void onCoreInitialize(CoreInitializeEvent e){
        THREAD_AMOUNT=Core.getInstance().getConfig().getInt("thread.var.amount",1);
        THREADPOOL=new ThreadPool(
                THREAD_AMOUNT,
                Core.getInstance().getConfig().getInt("thread.var.queue-size",512),
                "Var Async",
                Thread.NORM_PRIORITY-1
        );

        onCoreReload(null);
    }
    private static void onCoreReload(CoreReloadEvent e){
        if(cleanupTask!=null)cleanupTask.cancel();

        cleanupTask=Bukkit.getScheduler().runTaskTimer(Core.getInstance(),()->{
            if(shouldStayLoadedVars.isEmpty()){
                cleanupVars();
                return;
            }

            final AtomicInteger remaining=new AtomicInteger(shouldStayLoadedVars.size());
            for(final Var var:new HashSet<>(shouldStayLoadedVars)){
                if(var.shouldStayLoaded==null){
                    if(remaining.decrementAndGet()==0)cleanupVars();
                    continue;
                }

                var.shouldStayLoaded.get(var).thenAccept(bool->{
                    if(!bool)shouldStayLoadedVars.remove(var);

                    if(remaining.decrementAndGet()==0)cleanupVars();
                });
            }

            cleanupVars();
        },Core.CLEANUP_INTERVAL,Core.CLEANUP_INTERVAL);
    }
    private static void onCoreDisable(CoreDisableEvent e){
        Core.shutdownExecutor(THREADPOOL);
    }

    //OTHERS
    /**
     * Nettoie les références faibles de Vars inutilisées pour éviter les fuites mémoire.
     * Appelé périodiquement par un scheduler Bukkit.
     */
    static void cleanupVars(){
        synchronized(vars){
            vars.entrySet().removeIf(entry->entry.getValue().get()==null);
        }
    }

    //METHODS (INSTANCES)

    //SAVE
    /**
     * Retourne si des modifications ont été faites depuis le dernier save.
     */
    public boolean isDirty(){
        return this.dirty;
    }
    /**
     * Définit l'état dirty de la variable.
     *
     * @param value true si l'objet doit être marqué comme modifié
     */
    public void setDirty(boolean value){
        this.dirty =value;
    }

    //PATH
    public@NotNull Path getPath(){
        return this.path;
    }
    public@NotNull String getStringPath(){
        return getPath().toString();
    }

    //DATA
    /**
     * Décharge les données de la RAM et libère la ressource (Cleaner).
     * Après appel, l'instance n'est plus utilisable.
     */
    public void unload(){
        final long nanoTime=System.nanoTime();

        synchronized(this.data){
            this.data.clear();
        }
        this.cleanable.clean();

        PerformanceTracker.increment(PerformanceTracker.Types.VAR,"unload",System.nanoTime()-nanoTime);
    }

    //ABSTRACT
    /**
     * Sauvegarde synchrone (bloquante). Dépréciée, car peut geler le serveur.
     * <p>
     * Note : cette méthode prend déjà en compte l'état {@link #isDirty()}.
     * Si l'objet n'est pas dirty, aucun write n'est effectué.
     */
    @Deprecated
    public abstract void saveSync();
    /**
     * Sauvegarde l'objet de manière asynchrone (non bloquante).
     * L'implémentation concrète dépend de la sous-classe (ex : {@link VarFile}, {@link VarSql}).
     * Retourne un {@link CompletableFuture} qui s'exécute en tâche de fond et
     * permet d'ajouter un callback (thenRun, thenAccept, etc.).
     * <p>
     * Note : cette méthode prend déjà en compte l'état {@link #isDirty()}.
     * Si l'objet n'est pas dirty, la sauvegarde est ignorée.
     *
     * @return un CompletableFuture terminé lorsque la sauvegarde est effectuée
     */
    public abstract@NotNull CompletableFuture<@Nullable Void>saveAsync();

    //VALUE
    /**
     * Retourne l'ensemble de toutes les clés actuellement présentes dans cette instance.
     *
     * @return un Set non nul des clés
     */
    public@NotNull Set<@NotNull String>getKeys(){
        synchronized(this.data){
            return this.data.keySet();
        }

    }
    /**
     * Vérifie si une clé donnée est présente dans cette instance.
     *
     * @param key la clé à tester
     * @return true si la clé existe, false sinon
     */
    public boolean contains(@NotNull String key){
        synchronized(this.data){
            return this.data.containsKey(key);
        }
    }
    /**
     * Supprime une clé et sa valeur associée.
     *
     * @param key la clé à supprimer
     */
    public void remove(@NotNull String key){
        final long nanoTime=System.nanoTime();

        synchronized(this.data){
            this.data.remove(key);
        }

        if(Bukkit.isPrimaryThread())Bukkit.getPluginManager().callEvent(new DataSetEvent(DataSetEventType.NONE,key,null));
        else Bukkit.getScheduler().runTask(Core.getInstance(),()->
                Bukkit.getPluginManager().callEvent(new DataSetEvent(DataSetEventType.NONE,key,null)));

        PerformanceTracker.increment(PerformanceTracker.Types.VAR,"remove",System.nanoTime()-nanoTime);
    }
    private void removeWithoutEvent(@NotNull String key){
        final long nanoTime=System.nanoTime();

        synchronized(this.data){
            this.data.remove(key);
        }
        PerformanceTracker.increment(PerformanceTracker.Types.VAR,"removeWithoutEvent",System.nanoTime()-nanoTime);
    }

    /**
     * Définit une valeur.
     * Déclenche également un {@link DataSetEvent} pour notifier les listeners
     * Bukkit d'un changement de valeur.
     * - Si value != null : la valeur est stockée avec son type.
     * - Si value == null : la clé est supprimée.
     * Marque aussi l'objet comme "dirty" (à sauvegarder).
     *
     * @param type le type de la valeur (VarSubType)
     * @param key la clé unique de la valeur
     * @param value la valeur à stocker, ou null pour supprimer
     * @param <V> le type de la valeur
     */
    public<V>void setValue(@NotNull VarSubType<V>type,@NotNull String key,@Nullable V value){
        final long nanoTime=System.nanoTime();

        if(value!=null){
            synchronized(this.data){
                this.data.put(key,new Object[]{value,type});
            }
        }else removeWithoutEvent(key);

        this.dirty=true;

        if(Bukkit.isPrimaryThread())Bukkit.getPluginManager().callEvent(new DataSetEvent(DataSetEventType.WRAPPER,key,value));
        else Bukkit.getScheduler().runTask(Core.getInstance(),()->
                Bukkit.getPluginManager().callEvent(new DataSetEvent(DataSetEventType.WRAPPER,key,value)));

        PerformanceTracker.increment(PerformanceTracker.Types.VAR,"setValue",System.nanoTime()-nanoTime);
    }

    /**
     * Récupère une valeur typée stockée dans l'instance.
     *
     * @param type le type attendu (permet de vérifier la compatibilité).
     * @param key la clé associée
     * @param <T> le type de retour
     * @return la valeur si présente et du bon type, sinon null
     */
    public@Nullable<T>T getValue(@NotNull VarSubType<@NotNull T>type,@NotNull String key){
        final long nanoTime=System.nanoTime();

        final Object[]values;
        synchronized(this.data){
            values=this.data.get(key);
        }
        if(values==null||!((Vars)values[1]).equals(type)){
            PerformanceTracker.increment(PerformanceTracker.Types.VAR,"getValue",System.nanoTime()-nanoTime);
            return null;
        }

        PerformanceTracker.increment(PerformanceTracker.Types.VAR,"getValue",System.nanoTime()-nanoTime);
        return(T)values[0];
    }
    /**
     * Récupère une valeur typée stockée dans la variable.
     * Si elle n'existe pas où est d'un autre type, retourne une valeur par défaut.
     *
     * @param type le type attendu
     * @param key la clé associée
     * @param def la valeur par défaut à retourner si absente
     * @param <T> le type de retour
     * @return la valeur stockée ou def si absente/invalide
     */
    public@NotNull<T>T getValue(@NotNull VarSubType<T>type,@NotNull String key,@NotNull T def){
        return Objects.requireNonNullElse(getValue(type,key),def);
    }

    //VALUE-MAP
    /**
     * Définit une Map.
     * Déclenche un {@link DataSetEvent}.
     * - Si map != null : stocke la map avec ses types (clé/valeur).
     * - Si map == null : supprime la clé.
     * Marque l'objet comme "dirty".
     *
     * @param mapType type de map (HashMap, ConcurrentHashMap, etc.)
     * @param keyType type des clés de la map
     * @param valueType type des valeurs de la map
     * @param key la clé associée
     * @param map la map à stocker (ou null pour supprimer)
     * @param <T> type des clés
     * @param <T2> type des valeurs
     * @param <M> type de la map
     */
    public<T,T2,M extends Map<T,T2>>void putMap(@NotNull MapType<M> mapType,@NotNull VarSubType<T>keyType,@NotNull VarSubType<T2>valueType,@NotNull String key,@Nullable M map){
        final long nanoTime=System.nanoTime();

        if(map!=null){
            synchronized(this.data){
                data.put(key,new Object[]{map,new MapVarType<>(mapType,keyType,valueType)});
            }
        }else removeWithoutEvent(key);

        this.dirty=true;

        if(Bukkit.isPrimaryThread())Bukkit.getPluginManager().callEvent(new DataSetEvent(DataSetEventType.MAP,key,map));
        else Bukkit.getScheduler().runTask(Core.getInstance(),()->
                Bukkit.getPluginManager().callEvent(new DataSetEvent(DataSetEventType.MAP,key,map)));

        PerformanceTracker.increment(PerformanceTracker.Types.VAR,"putMap",System.nanoTime()-nanoTime);
    }

    /**
     * Récupère une Map stockée si elle correspond au type attendu.
     *
     * @param mapType type de map attendu
     * @param keyType type attendu des clés
     * @param valueType type attendu des valeurs
     * @param key la clé associée
     * @param <T> type des clés
     * @param <T2> type des valeurs
     * @param <M> type de la map
     * @return la map si présente et valide, sinon null
     */
    public@Nullable<T,T2,M extends Map<T,T2>>M getMap(@NotNull MapType<M>mapType,@NotNull VarSubType<T>keyType,@NotNull VarSubType<T2>valueType,@NotNull String key){
        final long nanoTime=System.nanoTime();

        final Object[]values;
        synchronized(this.data){
            values=this.data.get(key);
        }


        PerformanceTracker.increment(PerformanceTracker.Types.VAR,"getMap",System.nanoTime()-nanoTime);

        return(values==null||!((Vars)values[1]).equals(new MapVarType<>(mapType,keyType,valueType)))?null:(M)values[0];
    }
    /**
     * Récupère une Map stockée ou retourne une valeur par défaut si absente.
     *
     * @param mapType type de map attendu
     * @param keyType type attendu des clés
     * @param valueType type attendu des valeurs
     * @param key la clé associée
     * @param def valeur par défaut à retourner si absente
     * @param <T> type des clés
     * @param <T2> type des valeurs
     * @param <M> type de la map
     * @return la map stockée ou def si absente/invalide
     */
    public@NotNull<T,T2,M extends Map<T,T2>>M getMap(@NotNull MapType<M>mapType,@NotNull VarSubType<T>keyType,@NotNull VarSubType<T2>valueType,@NotNull String key,@NotNull M def){
        return Objects.requireNonNullElse(getMap(mapType,keyType,valueType,key),def);
    }
}