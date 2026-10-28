package fr.nexus.api.gui;

import com.cjcrafter.foliascheduler.TaskImplementation;
import fr.nexus.Core;
import fr.nexus.system.internal.performanceTracker.PerformanceTracker;
import fr.nexus.api.gui.panels.GuiPage;
import fr.nexus.api.gui.panels.GuiSlider;
import fr.nexus.api.listeners.Listeners;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

@SuppressWarnings({"unused","UnusedReturnValue"})
class GuiListeners{
    private static final ConcurrentHashMap<UUID, Long> CLOSE_PROTECTION_MAP = new ConcurrentHashMap<>();
    private static final long PROTECTION_DURATION_MS = 150L;

    static{
        Listeners.register(InventoryClickEvent.class,GuiListeners::onInventoryClick);
        Listeners.register(InventoryDragEvent.class,GuiListeners::onInventoryDrag);
        Listeners.register(InventoryCloseEvent.class,GuiListeners::onInventoryClose);
        Listeners.register(InventoryOpenEvent.class,GuiListeners::onInventoryOpen);
    }

    //METHODS (STATICS)
    private static void onInventoryClick(InventoryClickEvent e){
        final UUID uuid = e.getWhoClicked().getUniqueId();
        final long now = System.currentTimeMillis();

        // SÉCURITÉ GLOBALE ANTI-DUPLI POST-FERMETURE : Annule tout clic pendant les ticks suivant une fermeture
        final Long closeTime = CLOSE_PROTECTION_MAP.get(uuid);
        if (closeTime != null) {
            if (now - closeTime < PROTECTION_DURATION_MS) {
                e.setCancelled(true);
                if (e.getWhoClicked() instanceof Player pl) {
                    pl.setItemOnCursor(org.bukkit.inventory.ItemStack.of(org.bukkit.Material.AIR));
                    pl.updateInventory();
                }
                return;
            } else {
                CLOSE_PROTECTION_MAP.remove(uuid);
            }
        }

        final Gui gui=GuiManager.getGui(e.getInventory());
        if(gui!=null){
            if(gui.isDestroyed()){
                e.setCancelled(true);
                return;
            }
        }

        if(e.isCancelled()&&e.getWhoClicked().getGameMode()!=GameMode.SPECTATOR)
            return;

        final long nanoTime=System.nanoTime();

        if(gui==null)return;

        if(e.getInventory().equals(e.getClickedInventory())&&gui.getEffectiveCooldown()!=null){
            final long millis=System.currentTimeMillis();
            final Long pMillis=gui.getCooldowns().get(uuid);

            if(pMillis==null)gui.getCooldowns().put(uuid,millis);
            else if(millis-pMillis<gui.getEffectiveCooldown()){
                e.setCancelled(true);
                return;
            }else gui.getCooldowns().put(uuid,millis);
        }

        if(gui.getGlobalClickEvent()!=null)
            gui.getGlobalClickEvent().accept(e);

        if(e.getInventory().equals(e.getClickedInventory())&&gui.getInventoryClickEvent()!=null)
            gui.getInventoryClickEvent().accept(e);

        final int slot=e.getRawSlot();

        //GUI-ITEM
        GuiItem guiItem=gui.getGuiItemAt(slot);
        if(guiItem!=null){
            final BiConsumer<InventoryClickEvent, Player> action = guiItem.getPlayerAction();
            if(action != null && e.getWhoClicked() instanceof Player player){
                fr.nexus.Core.getServerImplementation().entity(player).run(()->{
                    if(player.isOnline()) action.accept(e, player);
                });
            }

            PerformanceTracker.increment(PerformanceTracker.Types.GUI,"onInventoryClick",System.nanoTime()-nanoTime);
            return;
        }

        //GUI-PAGE
        for(final GuiPage guiPage:gui.getGuiPages().values()){
            guiItem=guiPage.getGuiItemAt(slot);
            if(guiItem!=null){
                final BiConsumer<InventoryClickEvent, Player> action = guiItem.getPlayerAction();
                if(action != null && e.getWhoClicked() instanceof Player player){
                    fr.nexus.Core.getServerImplementation().entity(player).run(()->{
                        if(player.isOnline()) action.accept(e, player);
                    });
                }

                PerformanceTracker.increment(PerformanceTracker.Types.GUI,"onInventoryClick",System.nanoTime()-nanoTime);
                return;
            }
        }

        //GUI-SLIDER
        for(final GuiSlider guiSlider:gui.getGuiSliders().values()){
            guiItem=guiSlider.getGuiItemAt(slot);
            if(guiItem!=null){
                final BiConsumer<InventoryClickEvent, Player> action = guiItem.getPlayerAction();
                if(action != null && e.getWhoClicked() instanceof Player player){
                    fr.nexus.Core.getServerImplementation().entity(player).run(()->{
                        if(player.isOnline()) action.accept(e, player);
                    });
                }

                PerformanceTracker.increment(PerformanceTracker.Types.GUI,"onInventoryClick",System.nanoTime()-nanoTime);
                return;
            }
        }

        PerformanceTracker.increment(PerformanceTracker.Types.GUI,"onInventoryClick",System.nanoTime()-nanoTime);
    }

    private static void onInventoryDrag(InventoryDragEvent e){
        final UUID uuid = e.getWhoClicked().getUniqueId();
        if (CLOSE_PROTECTION_MAP.containsKey(uuid)) {
            e.setCancelled(true);
            return;
        }

        if (e.isCancelled()&&e.getWhoClicked().getGameMode() != GameMode.SPECTATOR) {
            return;
        }

        final long nanoTime=System.nanoTime();

        final Gui gui=GuiManager.getGui(e.getInventory());
        if(gui!=null&&gui.getDragEvent()!=null)
            gui.getDragEvent().accept(e);

        PerformanceTracker.increment(PerformanceTracker.Types.GUI,"onInventoryDrag",System.nanoTime()-nanoTime);
    }

    private static void onInventoryClose(InventoryCloseEvent e){
        final long nanoTime=System.nanoTime();

        // Enregistre l'horodatage exact de la fermeture pour bloquer les clics suivants
        if (e.getPlayer() instanceof Player pl) {
            CLOSE_PROTECTION_MAP.put(pl.getUniqueId(), System.currentTimeMillis());
            pl.setItemOnCursor(org.bukkit.inventory.ItemStack.of(org.bukkit.Material.AIR));
            pl.updateInventory();
        }

        final Inventory inv=e.getInventory();

        final Gui gui=GuiManager.getGui(inv);
        if(gui!=null){
            if(gui.getCloseEvent()!=null)gui.getCloseEvent().accept(e);

            if(inv.getViewers().size()-1<=0){
                GuiManager.removeReference(gui);

                if(gui.getReuse()==null&&gui.isDeleteOnClose()){
                    gui.destroy();
                }else{
                    final GuiConsumer guiConsumer=gui.getActiveGuiTickConsumer();
                    if(guiConsumer!=null&&guiConsumer.getTask()!=null){
                        guiConsumer.getTask().cancel();
                        guiConsumer.setTask(null);
                    }
                }
            }

            gui.getCooldowns().remove(e.getPlayer().getUniqueId());
        }

        PerformanceTracker.increment(PerformanceTracker.Types.GUI,"onInventoryClose",System.nanoTime()-nanoTime);
    }

    private static void onInventoryOpen(InventoryOpenEvent e){
        if(e.isCancelled())return;

        final long nanoTime=System.nanoTime();

        final Gui gui=GuiManager.getGui(e.getInventory());
        if(gui!=null){
            GuiManager.addReference(gui);

            if(e.getInventory().getViewers().size()==1){
                final GuiConsumer guiConsumer=gui.getActiveGuiTickConsumer();
                if(guiConsumer!=null&&guiConsumer.getTask()==null){
                    final TaskImplementation<?>task=Core.getServerImplementation().global().runAtFixedRate(()->{
                        final Gui gui2=guiConsumer.getWeakReference().get();
                        if(gui2!=null)guiConsumer.getConsumer().accept(gui2);
                    },guiConsumer.getTick(),guiConsumer.getTick());

                    guiConsumer.setTask(task);
                }
            }
        }

        PerformanceTracker.increment(PerformanceTracker.Types.GUI,"onInventoryOpen",System.nanoTime()-nanoTime);
    }
}