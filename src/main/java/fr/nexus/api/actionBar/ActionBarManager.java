package fr.nexus.api.actionBar;

import fr.nexus.Core;
import fr.nexus.api.listeners.core.CoreReloadEvent;
import fr.nexus.api.listeners.Listeners;
import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class ActionBarManager{
    //VARIABLES (STATICS)
    private static@Nullable BukkitTask cleanupTask;
    private static final@NotNull Set<@NotNull ActionBar>actionBarReferences=new HashSet<>();
    private static final@NotNull ConcurrentHashMap<@NotNull UUID,@NotNull WeakReference<ActionBar>>actionBars=new ConcurrentHashMap<>();
    static{
        Listeners.register(CoreReloadEvent.class,ActionBarManager::onCoreReload,EventPriority.LOWEST);
    }

    //METHODS (STATICS)

    //CLEANUP
    static void cleanupActionBars(){
        actionBars.entrySet().removeIf(entry->entry.getValue().get()==null);
    }

    //GUI
    public static@NotNull ActionBar getActionBar(@NotNull UUID uuid){
        final WeakReference<ActionBar>weakActionBar=actionBars.get(uuid);
        final ActionBar actionBar=weakActionBar!=null?weakActionBar.get():null;
        return actionBar!=null?actionBar:new ActionBar(uuid);
    }

    static void addActionBar(@NotNull UUID uuid,@NotNull ActionBar actionBar){
        actionBars.put(uuid,new WeakReference<>(actionBar));
    }
    static void removeActionBar(@NotNull UUID uuid){
        actionBars.remove(uuid);
    }

    //REFERENCES
    static@NotNull Set<@NotNull ActionBar>getGuiReferences(){
        return actionBarReferences;
    }
    static void addReference(@NotNull ActionBar actionBar){
        actionBarReferences.add(actionBar);
    }
    static void removeReference(@NotNull ActionBar actionBar){
        actionBarReferences.remove(actionBar);
    }

    //LISTENER
    private static void onCoreReload(CoreReloadEvent e){
        if(cleanupTask!=null)cleanupTask.cancel();

        cleanupTask=Bukkit.getScheduler().runTaskTimer(Core.getInstance(),ActionBarManager::cleanupActionBars,Core.CLEANUP_INTERVAL,Core.CLEANUP_INTERVAL);
    }
}