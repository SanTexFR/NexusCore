//package fr.nexus.listeners;
//
//import fr.nexus.Core;
//import fr.nexus.performanceTracker.PerformanceTracker;
//import org.bukkit.event.Event;
//import org.bukkit.event.EventPriority;
//import org.bukkit.event.Listener;
//import org.bukkit.plugin.PluginManager;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.*;
//import java.util.List;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.function.Consumer;
//
//@SuppressWarnings({"unused","UnusedReturnValue","unchecked"})
//public class Listeners implements Listener{
//    //VARIABLES
//    private static final@NotNull ConcurrentHashMap<@NotNull String,@NotNull List<@NotNull Consumer<?extends@NotNull Event>>>eventsRegistered=new ConcurrentHashMap<>();
//
//    //METHODS
//
//    //REGISTER
//    /**
//     * Enregistre un listener avec une priorité par défaut ({@link EventPriority#NORMAL}).
//     * Exemple :
//     * <pre>{@code
//     * Listeners.register(PlayerJoinEvent.class, e -> {
//     *     e.getPlayer().sendMessage("Bienvenue !");
//     * });
//     * }</pre>
//     *
//     * @param e    classe de l'événement Bukkit
//     * @param consumer lambda à exécuter quand l'événement survient
//     * @param <E>      type de l'événement
//     */
//    public static<E extends@NotNull Event>void register(@NotNull Class<E>e,@NotNull Consumer<E>consumer){
//        register(e,consumer,EventPriority.NORMAL);
//    }
//    /**
//     * Enregistre un listener avec une priorité spécifique.
//     * - Si aucun handler n'existe encore pour (eventClass, priority),
//     *   il est créé et enregistré auprès du {@link PluginManager}.
//     * - Le consumer est ajouté à la liste des callbacks à exécuter.
//     *
//     * @param e    classe de l'événement Bukkit
//     * @param consumer lambda à exécuter
//     * @param priority priorité d'exécution
//     * @param <E>      type de l'événement
//     */
//    public static<E extends@NotNull Event>void register(@NotNull Class<E>e,@NotNull Consumer<E>consumer,@NotNull EventPriority priority){
//        final long nanoTime=System.nanoTime();
//
//        final String key=buildKey(e,priority);
//
//        //REGISTER EVENT
//        if(!eventsRegistered.containsKey(key)){
//            final Listeners globalListener=Core.getListeners();
//            globalListener.registerEventHandler(e,Core.getInstance().getServer().getPluginManager(),globalListener, priority);
//        }
//
//        //ADD CONSUMER
//        eventsRegistered.computeIfAbsent(key,k->Collections.synchronizedList(new ArrayList<>()))
//                .add(consumer);
//
//        PerformanceTracker.increment(PerformanceTracker.Types.LISTENER,"register",System.nanoTime()-nanoTime);
//    }
//
//    //GESTIONER METHODS
//    private<T extends@NotNull Event>void registerEventHandler(@NotNull Class<T>eventClass,@NotNull PluginManager pluginManager,@NotNull Listener listener,@NotNull EventPriority priority){
//        final String key=buildKey(eventClass,priority);
//
//        pluginManager.registerEvent(eventClass,listener,priority,(l,event)->{
//            if(!eventClass.isInstance(event))return;
//
//            final List<Consumer<?extends Event>>consumers=eventsRegistered.get(key);
//            if(consumers==null)return;
//
//            final long nanoTime=System.nanoTime();
//
//            for(Consumer<?extends Event>consumer:consumers)
//                ((Consumer<Event>)consumer).accept(event);
//
//            PerformanceTracker.increment(PerformanceTracker.Types.LISTENER,key,System.nanoTime()-nanoTime);
//        },Core.getInstance(),false);
//    }
//
//    private static@NotNull String buildKey(@NotNull Class<?extends@NotNull Event>eventClass,@NotNull EventPriority priority){
//        return String.join("/",eventClass.getName(),priority.name());
//    }
//}