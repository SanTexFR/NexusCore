package fr.nexus.api.actionBar;

import fr.nexus.api.listeners.core.CoreCleanupEvent;
import fr.nexus.api.listeners.Listeners;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class ActionBarManager{
    //VARIABLES (STATICS)
    private static final@NotNull Set<@NotNull ActionBar>actionBarReferences=new HashSet<>();
    private static final@NotNull ConcurrentHashMap<@NotNull UUID,@NotNull WeakReference<ActionBar>>actionBars=new ConcurrentHashMap<>();
    static{
        Listeners.register(CoreCleanupEvent.class,ActionBarManager::onCoreCleanup,EventPriority.LOWEST);
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
    private static void onCoreCleanup(CoreCleanupEvent e){
        cleanupActionBars();
    }
}