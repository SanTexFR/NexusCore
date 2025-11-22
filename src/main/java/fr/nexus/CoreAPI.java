package fr.nexus;

import fr.nexus.api.listeners.server.ServerLoadEarlyEvent;
import fr.nexus.api.listeners.server.ServerStopEvent;
import fr.nexus.api.listeners.server.ServerStartEvent;
import fr.nexus.system.internal.performanceTracker.PerformanceTracker;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class CoreAPI{
    //VARIABLES (STATICS)
    private static final@NotNull Logger logger=Logger.getLogger(CoreAPI.class.getName());
    private static@NotNull State state=State.NONE;

    //EVENTS
    /**
     * Charge l'API et déclenche {@link ServerLoadEarlyEvent}.
     * <p>
     * Cette méthode doit être appelée une seule fois et
     * avant {@link #start()} ou {@link #stop()}.
     * </p>
     */
    public static void load(){
        if(state!=State.NONE){
            logger.severe("❌ load() ne peut être appelé qu'une seule fois et avant start() / stop().");
            return;
        }

        //LOAD-EVENT
        final long nanoTime=System.nanoTime();

        if(Bukkit.isPrimaryThread())Bukkit.getPluginManager().callEvent(new ServerLoadEarlyEvent());
        else Core.getServerImplementation().global().run(()->
                Bukkit.getPluginManager().callEvent(new ServerLoadEarlyEvent()));

        PerformanceTracker.increment(PerformanceTracker.Types.LISTENER,"ServerLoadEarlyEvent",System.nanoTime()-nanoTime);

        state=State.LOADED;
        logger.info("✅ CoreAPI.load() exécuté avec succès !");
    }
    /**
     * Démarre l'API et déclenche {@link ServerStartEvent}.
     * <p>
     * Doit être appelé après {@link #load()} et uniquement une fois.
     * </p>
     */
    public static void start(){
        if(state!=State.LOADED){
            logger.severe("❌ start() doit être appelé après load() et seulement une fois.");
            return;
        }

        //START-EVENT
        Core.getServerImplementation().global().runDelayed(()->{
            final long nanoTime=System.nanoTime();

            if(Bukkit.isPrimaryThread())Bukkit.getPluginManager().callEvent(new ServerStartEvent());
            else Core.getServerImplementation().global().run(()->
                    Bukkit.getPluginManager().callEvent(new ServerStartEvent()));

            PerformanceTracker.increment(PerformanceTracker.Types.LISTENER,"ServerStartEvent",System.nanoTime()-nanoTime);


            state=State.STARTED;
            logger.info("✅ CoreAPI.start() exécuté avec succès !");
        },1L);
    }
    /**
     * Arrête l'API et déclenche {@link ServerStopEvent}.
     * <p>
     * Doit être appelé après {@link #start()} et uniquement une fois.
     * </p>
     */
    public static void stop(){
        if(state!=State.STARTED){
            logger.severe("❌ stop() doit être appelé après start() et seulement une fois.");
            return;
        }

        //STOP-EVENT
        final long nanoTime=System.nanoTime();

        if(Bukkit.isPrimaryThread())Bukkit.getPluginManager().callEvent(new ServerStopEvent());
        else Core.getServerImplementation().global().run(()->
                Bukkit.getPluginManager().callEvent(new ServerStopEvent()));

        PerformanceTracker.increment(PerformanceTracker.Types.LISTENER,"ServerStopEvent",System.nanoTime()-nanoTime);


        state=State.STOPPED;
        logger.info("✅ CoreAPI.stop() exécuté avec succès !");
    }

    //METHODS (STATICS)
    /**
     * Retourne l'état courant de l'API.
     *
     * @return l'état actuel de {@link CoreAPI}
     */
    public static@NotNull State getState(){
        return state;
    }

    //INNER CLASS
    /**
     * États possibles de l'API, représentant le cycle de vie
     * du serveur.
     */
    public enum State{
        /** État initial, avant tout chargement. */
        NONE,
        /** Après exécution de {@link #load()}. */
        LOADED,
        /** Après exécution de {@link #start()}. */
        STARTED,
        /** Après exécution de {@link #stop()}. */
        STOPPED
    }
}