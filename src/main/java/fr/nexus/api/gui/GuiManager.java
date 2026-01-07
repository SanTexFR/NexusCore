package fr.nexus.api.gui;

import fr.nexus.api.listeners.core.CoreCleanupEvent;
import fr.nexus.api.listeners.Listeners;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class GuiManager{
    //VARIABLES (STATICS)
    public static final@NotNull ConcurrentHashMap<@NotNull String,@NotNull Gui>reuseGuis=new ConcurrentHashMap<>();
    public static final@NotNull Set<@NotNull Gui>guiReferences=new HashSet<>();
    public static final@NotNull ConcurrentHashMap<@NotNull Inventory,@NotNull WeakReference<Gui>>guis=new ConcurrentHashMap<>();
    static{
        Listeners.register(CoreCleanupEvent.class,GuiManager::onCoreCleanup,EventPriority.LOWEST);
    }

    //METHODS (STATICS)

    //CLEANUP
    static void cleanupGuis(){
        guis.entrySet().removeIf(entry->entry.getValue().get()==null);
        reuseGuis.clear();
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
    private static void onCoreCleanup(CoreCleanupEvent e){
        cleanupGuis();
    }
}
