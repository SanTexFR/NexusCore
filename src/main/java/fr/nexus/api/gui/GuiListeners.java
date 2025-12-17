package fr.nexus.api.gui;

import com.cjcrafter.foliascheduler.TaskImplementation;
import fr.nexus.Core;
import fr.nexus.system.internal.performanceTracker.PerformanceTracker;
import fr.nexus.api.gui.panels.GuiPage;
import fr.nexus.api.gui.panels.GuiSlider;
import fr.nexus.api.listeners.Listeners;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

@SuppressWarnings({"unused","UnusedReturnValue"})
class GuiListeners{
    //VARIABLES (STATICS)
    static{
        Listeners.register(InventoryClickEvent.class,GuiListeners::onInventoryClick);
        Listeners.register(InventoryDragEvent.class,GuiListeners::onInventoryDrag);
        Listeners.register(InventoryCloseEvent.class,GuiListeners::onInventoryClose);
        Listeners.register(InventoryOpenEvent.class,GuiListeners::onInventoryOpen);
    }

    //METHODS (STATICS)
    private static void onInventoryClick(InventoryClickEvent e){
        if(e.isCancelled())return;

        final long nanoTime=System.nanoTime();

        final Gui gui=GuiManager.getGui(e.getInventory());
        if(gui==null)return;

        if(e.getInventory().equals(e.getClickedInventory())&&gui.getEffectiveCooldown()!=null){
            if(e.isCancelled())return;

            final UUID uuid=e.getWhoClicked().getUniqueId();
            final long millis=System.currentTimeMillis();
            final Long pMillis=gui.getCooldowns().get(e.getWhoClicked().getUniqueId());

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
            if(guiItem.getAction()!=null)guiItem.getAction().accept(e);

            PerformanceTracker.increment(PerformanceTracker.Types.GUI,"onInventoryClick",System.nanoTime()-nanoTime);
            return;
        }

        //GUI-PAGE
        for(final GuiPage guiPage:gui.getGuiPages().values()){
            guiItem=guiPage.getGuiItemAt(slot);
            if(guiItem!=null){
                if(guiItem.getAction()!=null)guiItem.getAction().accept(e);

                PerformanceTracker.increment(PerformanceTracker.Types.GUI,"onInventoryClick",System.nanoTime()-nanoTime);
                return;
            }
        }

        //GUI-SLIDER
        for(final GuiSlider guiSlider:gui.getGuiSliders().values()){
            guiItem=guiSlider.getGuiItemAt(slot);
            if(guiItem!=null){
                if(guiItem.getAction()!=null)guiItem.getAction().accept(e);

                PerformanceTracker.increment(PerformanceTracker.Types.GUI,"onInventoryClick",System.nanoTime()-nanoTime);
                return;
            }
        }

        PerformanceTracker.increment(PerformanceTracker.Types.GUI,"onInventoryClick",System.nanoTime()-nanoTime);
    }
    private static void onInventoryDrag(InventoryDragEvent e){
        if(e.isCancelled())return;

        final long nanoTime=System.nanoTime();

        final Gui gui=GuiManager.getGui(e.getInventory());
        if(gui!=null&&gui.getDragEvent()!=null)
            gui.getDragEvent().accept(e);

        PerformanceTracker.increment(PerformanceTracker.Types.GUI,"onInventoryDrag",System.nanoTime()-nanoTime);
    }
    private static void onInventoryClose(InventoryCloseEvent e){
        final long nanoTime=System.nanoTime();

        final Inventory inv=e.getInventory();

        final Gui gui=GuiManager.getGui(inv);
        if(gui!=null){
            if(inv.getViewers().size()-1<=0){
                GuiManager.removeReference(gui);

                final GuiConsumer guiConsumer=gui.getActiveGuiTickConsumer();
                if(guiConsumer!=null&&guiConsumer.getTask()!=null){
                    guiConsumer.getTask().cancel();
                    guiConsumer.setTask(null);
                }
            }
            if(gui.getCloseEvent()!=null)gui.getCloseEvent().accept(e);
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