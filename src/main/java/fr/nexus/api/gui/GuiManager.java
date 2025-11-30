package fr.nexus.api.gui;

import com.cjcrafter.foliascheduler.TaskImplementation;
import fr.nexus.Core;
import fr.nexus.api.listeners.core.CoreInitializeEvent;
import fr.nexus.api.listeners.core.CoreReloadEvent;
import fr.nexus.api.listeners.Listeners;
import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class GuiManager{
    //VARIABLES (STATICS)
    private static@Nullable TaskImplementation<?>cleanupTask;
    public static final@NotNull Set<@NotNull Gui>guiReferences=new HashSet<>();
    public static final@NotNull ConcurrentHashMap<@NotNull Inventory,@NotNull WeakReference<Gui>>guis=new ConcurrentHashMap<>();
    static{
        Listeners.register(CoreInitializeEvent.class,GuiManager::onCoreInitialize,EventPriority.LOWEST);
        Listeners.register(CoreReloadEvent.class,GuiManager::onCoreReload,EventPriority.LOWEST);
    }

    //METHODS (STATICS)

    //CLEANUP
    static void cleanupGuis(){
        guis.entrySet().removeIf(entry->entry.getValue().get()==null);
    }

    //GUI
    public static@Nullable Gui getGui(@NotNull Inventory inventory){
        final WeakReference<Gui>weakGui=guis.get(inventory);
        return(weakGui!=null)?weakGui.get():null;
    }

    static void addGui(@NotNull Inventory inventory,@NotNull Gui gui){
        guis.put(inventory,new WeakReference<>(gui));
    }
    static void removeGui(@NotNull Inventory inventory){
        guis.remove(inventory);
    }

    //REFERENCES
    static @NotNull Set<@NotNull Gui>getGuiReferences(){
        return guiReferences;
    }
    static void addReference(@NotNull Gui gui){
        guiReferences.add(gui);
    }
    static void removeReference(@NotNull Gui gui){
        guiReferences.remove(gui);
    }

    //LISTENER
    private static void onCoreInitialize(CoreInitializeEvent e){
        onCoreReload(null);
    }
    private static void onCoreReload(CoreReloadEvent e){
        if(cleanupTask!=null)cleanupTask.cancel();

        cleanupTask=Core.getServerImplementation().global().runAtFixedRate(GuiManager::cleanupGuis,Core.CLEANUP_INTERVAL,Core.CLEANUP_INTERVAL);
    }
}
