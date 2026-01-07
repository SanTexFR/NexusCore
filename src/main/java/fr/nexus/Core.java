package fr.nexus;

import com.cjcrafter.foliascheduler.FoliaCompatibility;
import com.cjcrafter.foliascheduler.ServerImplementation;
import com.cjcrafter.foliascheduler.TaskImplementation;
import fr.nexus.api.listeners.core.CoreCleanupEvent;
import fr.nexus.api.listeners.core.CoreDisableEvent;
import fr.nexus.api.listeners.core.CoreInitializeEvent;
import fr.nexus.api.listeners.core.CoreReloadEvent;
import fr.nexus.api.listeners.Listeners;
import fr.nexus.api.var.types.VarTypes;
import fr.nexus.system.ClazzInitializer;
import fr.nexus.system.Logger;
import fr.nexus.system.ThreadPool;
import fr.nexus.system.Updater;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class Core extends JavaPlugin{
    //VARIABLES (STATICS)
    private static ServerImplementation serverImplementation;
    private static Logger logger;

    private static long CLEANUP_INTERVAL;
    private static@Nullable TaskImplementation<?> cleanupTask;
    private static@NotNull UUID lastCleanupUUID=UUID.randomUUID();

    //VARIABLES (STATICS)
    private static Core instance;
    private static Listeners listeners;
    private static final@NotNull Cleaner cleaner=Cleaner.create();

    private static final@NotNull UUID sessionUUID=UUID.randomUUID();

    //METHODS (OVERRIDE)
    @Override
    public void onEnable(){
        //INSTANCE
        serverImplementation=new FoliaCompatibility(this).getServerImplementation();
        instance=this;
        logger=new Logger(this,Core.class);


        //CONFIG
        saveDefaultConfig();

        if(getConfig().getBoolean("generate-vartypes-file",false)){
            try{
                VarTypes.generateFileTypes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        //LISTENERS
        final Listeners listener=new Listeners();
        listeners=listener;
        getServer().getPluginManager().registerEvents(listener,this);

        //INITIALIZER
        ClazzInitializer.initialize();

        //INITIALIZE
        Bukkit.getPluginManager().callEvent(new CoreInitializeEvent());
        Listeners.register(CoreReloadEvent.class,Listeners::onCoreReload);

        //UPDATER
        Updater.checkForUpdates();

        Listeners.register(CoreReloadEvent.class,Core::onCoreReload);
    }

    @Override
    public void onDisable(){
        final PluginManager pluginManager=Bukkit.getPluginManager();
        pluginManager.callEvent(new CoreDisableEvent());
        shutdownExecutor(Listeners.THREADPOOL);
        pluginManager.callEvent(new CoreCleanupEvent());
    }
    public static void shutdownExecutor(@NotNull ThreadPool threadPool){
        try{
            threadPool.shutdown();
            threadPool.awaitTermination(5,TimeUnit.SECONDS);
        }catch(InterruptedException e){
            logger.severe("shutdownExecutor issue ("+threadPool.getPrefix()+"): {}",e.getMessage());
        }
    }

    //METHODS (STATICS)
    public static ServerImplementation getServerImplementation(){
        return serverImplementation;
    }
    public static@NotNull Core getInstance(){
        return instance;
    }
    public@NotNull ClassLoader getClazzLoader(){
        return super.getClassLoader();
    }
    public static@NotNull Listeners getListeners(){
        return listeners;
    }
    public static@NotNull UUID getSessionUUID(){
        return sessionUUID;
    }
    public static @NotNull Cleaner getCleaner(){
        return cleaner;
    }
    public static@NotNull UUID getLastCleanupUUID(){
        return lastCleanupUUID;
    }
    public static void setLastCleanupUUID(@NotNull UUID uuid){
        lastCleanupUUID=uuid;
    }

    public static void reload(boolean safe){
        getInstance().reloadConfig();
        getInstance().saveDefaultConfig();

        if(Bukkit.isPrimaryThread())Bukkit.getPluginManager().callEvent(new CoreReloadEvent(safe));
        else Core.getServerImplementation().global().run(()->Bukkit.getPluginManager().callEvent(new CoreReloadEvent(safe)));
    }


    private static void onCoreInitialize(CoreInitializeEvent e){
        onCoreReload(null);
    }
    private static void onCoreReload(CoreReloadEvent e){
        CLEANUP_INTERVAL=getInstance().getConfig().getLong("cache.cleanupInterval",6000);

        if(cleanupTask!=null)cleanupTask.cancel();
        cleanupTask=Core.getServerImplementation().global().runAtFixedRate(()->{
            if(Bukkit.isPrimaryThread())Bukkit.getPluginManager().callEvent(new CoreCleanupEvent());
            else Core.getServerImplementation().global().run(()->Bukkit.getPluginManager().callEvent(new CoreCleanupEvent()));
        },Core.CLEANUP_INTERVAL,Core.CLEANUP_INTERVAL);
    }
}