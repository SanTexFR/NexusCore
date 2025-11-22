package fr.nexus.api.listeners;

import fr.nexus.Core;
import fr.nexus.system.ThreadPool;
import fr.nexus.api.listeners.core.CoreReloadEvent;
import fr.nexus.api.listeners.events.CoreEvent;
import fr.nexus.system.internal.performanceTracker.PerformanceTracker;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings({"unused", "UnusedReturnValue", "unchecked"})
public class Listeners implements Listener{
    //VARIABLES (STATICS)

    //SYNC
    public static final@NotNull ConcurrentHashMap<@NotNull String,@NotNull List<@NotNull Consumer<?extends@NotNull Event>>>syncEventsRegistered=new ConcurrentHashMap<>();

    //ASYNC
    private static boolean customAsyncBukkitEventEnabled;

    public static ThreadPool THREADPOOL;

    public static final@NotNull ConcurrentHashMap<@NotNull String,@NotNull List<@NotNull ConsumerWrapper<CoreEvent<?>,Object>>>asyncEventsRegistered=new ConcurrentHashMap<>();
    private static final@NotNull Set<@NotNull Class<?>>registeredCoreBridges=ConcurrentHashMap.newKeySet();
    private static final@NotNull Set<@NotNull Object>activeEventKeys=ConcurrentHashMap.newKeySet();

    //CONSTRUCTOR
    public Listeners(){
        THREADPOOL=new ThreadPool(
                Core.getInstance().getConfig().getInt("thread.listeners.amount",1),
                Core.getInstance().getConfig().getInt("thread.listeners.queue-size",240),
                "Listeners Async",
                Thread.NORM_PRIORITY-1
        );

        onCoreReload(null);
    }

    //LISTENERS
    public static void onCoreReload(CoreReloadEvent e){
        customAsyncBukkitEventEnabled=Core.getInstance().getConfig().getBoolean("enableCustomAsyncBukkitEventApi",false);
    }

    //METHODS (STATICS)

    //SYNC
    public static<E extends@NotNull Event>void register(@NotNull Class<E>e,@NotNull Consumer<E>consumer){
        register(e,consumer,EventPriority.NORMAL);
    }
    public static<E extends@NotNull Event>void register(@NotNull Class<E>e,@NotNull Consumer<E>consumer,@NotNull EventPriority priority){
        final long nanoTime=System.nanoTime();

        final String key=buildKey(e,priority);

        //REGISTER EVENT
        if(!syncEventsRegistered.containsKey(key)){
            final Listeners globalListener=Core.getListeners();
            globalListener.registerEventHandler(e,Core.getInstance().getServer().getPluginManager(),globalListener, priority);
        }

        //ADD CONSUMER
        syncEventsRegistered.computeIfAbsent(key,k->Collections.synchronizedList(new ArrayList<>()))
                .add(consumer);

        PerformanceTracker.increment(PerformanceTracker.Types.LISTENER,"register",System.nanoTime()-nanoTime);
    }

    private<T extends@NotNull Event>void registerEventHandler(@NotNull Class<T>eventClass,@NotNull PluginManager pluginManager,@NotNull Listener listener,@NotNull EventPriority priority){
        final String key=buildKey(eventClass,priority);

        pluginManager.registerEvent(eventClass,listener,priority,(l,event)->{
            if(!eventClass.isInstance(event))return;

            final List<Consumer<?extends Event>>consumers=syncEventsRegistered.get(key);
            if(consumers==null)return;

            final long nanoTime=System.nanoTime();

            for(Consumer<?extends Event>consumer:consumers)
                ((Consumer<Event>)consumer).accept(event);

            PerformanceTracker.increment(PerformanceTracker.Types.LISTENER,key,System.nanoTime()-nanoTime);
        },Core.getInstance(),false);
    }

    private static@NotNull String buildKey(@NotNull Class<?extends@NotNull Event>eventClass,@NotNull EventPriority priority){
        return String.join("/",eventClass.getName(),priority.name());
    }

    //ASYNC
    public static<E extends@NotNull CoreEvent<?>>void registerAsync(
            @NotNull Class<@NotNull E>coreEventClass,
            @Nullable Function<@NotNull E,@Nullable Object>syncConsumer,
            @NotNull BiFunction<@NotNull E,@Nullable Object,@Nullable Object>asyncConsumer,
            @Nullable BiFunction<@NotNull E,@Nullable Object,@Nullable Object>postSyncConsumer,
            @NotNull BiConsumer<@NotNull E,@Nullable Object>syncResultConsumer){

        final ConsumerWrapper<E,Object>wrapper=new ConsumerWrapper<>(syncConsumer,asyncConsumer,postSyncConsumer,syncResultConsumer);
        asyncEventsRegistered.computeIfAbsent(coreEventClass.getName(), k->new CopyOnWriteArrayList<>())
                .add((ConsumerWrapper<CoreEvent<?>,Object>)wrapper);

    }

    public static<B extends@NotNull Event,C extends@NotNull CoreEvent<@NotNull B>>void registerCoreBridge(@NotNull Class<B>bukkitEventClass,@NotNull Function<B,C>coreEventConstructor) {
        if(registeredCoreBridges.contains(bukkitEventClass))return;
        registeredCoreBridges.add(bukkitEventClass);

        final PluginManager pm=Core.getInstance().getServer().getPluginManager();
        pm.registerEvent(bukkitEventClass,Core.getListeners(),EventPriority.HIGHEST,(listener,event)->{
            if(!customAsyncBukkitEventEnabled||!bukkitEventClass.isInstance(event))return;
            final B bukkitEvent=bukkitEventClass.cast(event);

            final CoreEvent<?>coreEvent=coreEventConstructor.apply(bukkitEvent);

            final List<ConsumerWrapper<CoreEvent<?>,Object>>consumers=asyncEventsRegistered.get(coreEvent.getClass().getName());
            if(consumers==null||consumers.isEmpty())return;

            if(bukkitEvent instanceof Cancellable cancellable)
                cancellable.setCancelled(true);

            callCoreEvent(coreEvent,consumers);
        },Core.getInstance(),false);
    }
    private static void callCoreEvent(@NotNull CoreEvent<?>coreEvent,@NotNull List<ConsumerWrapper<CoreEvent<?>,Object>>consumers){
        final Object key=coreEvent.getKey();
        if(!activeEventKeys.add(key))
            return;

        final List<Runnable>runnables=Collections.synchronizedList(new ArrayList<>());
        final AtomicInteger remaining=new AtomicInteger(consumers.size());

        for(ConsumerWrapper<CoreEvent<?>,Object>rawWrapper:consumers){
            final ConsumerWrapper<CoreEvent<?>,Object>consumer=rawWrapper;
            final Object[]obj={null};

            if(consumer.syncConsumer!=null)obj[0]=consumer.syncConsumer.apply(coreEvent);

            if(coreEvent.isAsyncCancelled()){
                executeRunnablesIfNeeded(remaining,coreEvent,runnables,key);
                continue;
            }

            THREADPOOL.execute(()->{
                obj[0]=consumer.asyncConsumer.apply(coreEvent,obj[0]);

                if(coreEvent.isAsyncCancelled()){
                    Core.getServerImplementation().global().run(()->executeRunnablesIfNeeded(remaining,coreEvent,runnables,key));
                    return;
                }
                Core.getServerImplementation().global().run(()->{
                    if(consumer.postSyncConsumer!=null)obj[0]=consumer.postSyncConsumer.apply(coreEvent,obj[0]);

                    if(coreEvent.isAsyncCancelled()){
                        executeRunnablesIfNeeded(remaining,coreEvent,runnables,key);
                        return;
                    }

                    runnables.add(()->consumer.syncResultConsumer.accept(coreEvent,obj[0]));

                    executeRunnablesIfNeeded(remaining,coreEvent,runnables,key);
                });
            });
        }
    }
    private static void executeRunnablesIfNeeded(@NotNull AtomicInteger remaining,@NotNull CoreEvent<?>coreEvent,@NotNull List<@NotNull Runnable>runnables,@NotNull Object key){
        if(remaining.decrementAndGet()==0){
            try{
                if(coreEvent.applyResult())runnables.forEach(Runnable::run);
            }finally{
                activeEventKeys.remove(key);
            }
        }
    }

    //INNER CLASS
    public record ConsumerWrapper<E,R>(
            @Nullable Function<@NotNull E,@Nullable R>syncConsumer,
            @NotNull BiFunction<@NotNull E,@Nullable R,@Nullable R>asyncConsumer,
            @Nullable BiFunction<@NotNull E,@Nullable R,@Nullable R>postSyncConsumer,
            @NotNull BiConsumer<@NotNull E,@Nullable R> syncResultConsumer
    ){}
}