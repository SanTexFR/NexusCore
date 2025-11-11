package fr.nexus.performanceTracker;

import fr.nexus.Core;
import fr.nexus.gui.Gui;
import fr.nexus.gui.GuiItem;
import fr.nexus.gui.panels.GuiPage;
import fr.nexus.itembuilder.ItemBuilder;
import fr.nexus.listeners.Listeners;
import fr.nexus.listeners.core.CoreInitializeEvent;
import fr.nexus.listeners.core.CoreReloadEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class PerformanceTrackerGui{
    //VARIABLES (STATICS)
    private static int UPDATE_INTERVAL;
    static{
        Listeners.register(CoreInitializeEvent.class,PerformanceTrackerGui::onCoreInitialize);
        Listeners.register(CoreReloadEvent.class,PerformanceTrackerGui::onCoreReload);
    }

    //METHODS (STATICS)

    //LISTENERS
    private static void onCoreInitialize(CoreInitializeEvent e){
        onCoreReload(null);
    }
    private static void onCoreReload(CoreReloadEvent e){
        UPDATE_INTERVAL=Math.min(100,Core.getInstance().getConfig().getInt("performanceTrackerGuiUpdateRate",1));
    }

    //PRIMARY
    public static void primaryGui(@NotNull Player p){
        //OTHERS
        final AtomicBoolean actualisation=new AtomicBoolean(false);

        //GUI
        final Gui gui=new Gui(3,Component.text("Menu des performances"));
        final WeakReference<Gui>weakGui=new WeakReference<>(gui);

        gui.setInventoryClickEvent(e2->e2.setCancelled(true));
        gui.setBackground(new GuiItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)));

        gui.setEffectiveCooldownMs(500L);

        //GUI-PAGE
        final GuiPage guiPage=new GuiPage(1,1,7,1);
        gui.addGuiPage("pagination",guiPage);
        guiPage.setBackground(new GuiItem(new ItemStack(Material.WHITE_STAINED_GLASS_PANE)));
        reloadPrimaryItems(weakGui,guiPage,p,actualisation);

        //GUI PAGE-ITEM
        gui.addGuiItem(1,2,new GuiItem(ItemBuilder.createItem(Material.ARROW).setDisplayName("←").build(),e2->{
            if(e2.getClick().equals(ClickType.DOUBLE_CLICK))return;
            guiPage.previous();
            guiPage.update();
        }));
        gui.addGuiItem(7,2,new GuiItem(ItemBuilder.createItem(Material.ARROW).setDisplayName("→").build(),e2->{
            if(e2.getClick().equals(ClickType.DOUBLE_CLICK))return;
            guiPage.next();
            guiPage.update();
        }));

        //UPDATE-ITEM
        updateActualisationButton(weakGui,null,p,actualisation,2);

        //OTHERS
        gui.generalUpdate();
        Bukkit.getScheduler().runTaskLater(Core.getInstance(),()->
                gui.display(p),4L);
    }
    private static void reloadPrimaryItems(@NotNull WeakReference<Gui>weakGui,@NotNull GuiPage guiPage,@NotNull Player p,@NotNull AtomicBoolean actualisation){
        final Gui gui=weakGui.get();
        if(gui==null)return;

        guiPage.clearGuiItems();

        int totalSyncCount=0;
        long totalSyncNanos=0;
        long totalSyncMillis=0;
        int totalSyncSeconds=0;

        int totalAsyncCount=0;
        long totalAsyncNanos=0;
        long totalAsyncMillis=0;
        int totalAsyncSeconds=0;

        for(final PerformanceTracker.Types type:PerformanceTracker.Types.values()){
            int syncCount=0;
            long syncNanos=0;

            int asyncCount=0;
            long asyncNanos=0;
            final Set<String>methods=PerformanceTracker.getMethods(type);
            for(final String method:methods){
                final PerformanceTracker.TrackerData trackerData=PerformanceTracker.get(type,method);
                if(trackerData==null)continue;

                syncCount+=trackerData.syncCount();
                syncNanos+=trackerData.syncTime();

                asyncCount+=trackerData.asyncCount();
                asyncNanos+=trackerData.asyncTime();
            }

            long syncMillis=syncNanos/1_000_000;
            int syncSeconds=(int)(syncNanos/1_000_000_000L);

            long asyncMillis=asyncNanos/1_000_000;
            int asyncSeconds=(int)(asyncNanos/1_000_000_000L);

            final ItemBuilder itemBuilder=ItemBuilder.createItem(Material.GRASS_BLOCK);
            itemBuilder.setDisplayName(type.name());

            setItemBuilderLore(itemBuilder,syncCount,syncNanos,syncMillis,syncSeconds,asyncCount,asyncNanos,asyncMillis,asyncSeconds);

            totalSyncCount+=syncCount;
            totalSyncNanos+=syncNanos;
            totalSyncMillis+=syncMillis;
            totalSyncSeconds+=syncSeconds;

            totalAsyncCount+=asyncCount;
            totalAsyncNanos+=asyncNanos;
            totalAsyncMillis+=asyncMillis;
            totalAsyncSeconds+=asyncSeconds;

            guiPage.addGuiItem(new GuiItem(itemBuilder.build(),e->
                secondaryGui(gui,weakGui,type,p,actualisation)));
        }

        final ItemBuilder itemBuilder=ItemBuilder.createItem(Material.PAPER);
        itemBuilder.setDisplayName("Stats globales | MSPT par joueur: "+(int)Math.round(Bukkit.getServer().getAverageTickTime()/Bukkit.getOnlinePlayers().size()));

        setItemBuilderLore(itemBuilder,totalSyncCount,totalSyncNanos,totalSyncMillis,totalSyncSeconds,totalAsyncCount,totalAsyncNanos,totalAsyncMillis,totalAsyncSeconds);

        gui.addGuiItem(4,0,new GuiItem(itemBuilder.build()));

        gui.generalUpdate();
    }

    //SECONDARY
    private static void secondaryGui(@NotNull Gui mainGui,@NotNull WeakReference<Gui>weakMainGui,@NotNull PerformanceTracker.Types type,@NotNull Player p,@NotNull AtomicBoolean actualisation){
        //GUI
        final Gui gui=new Gui(5,Component.text("Performance: "+type.name()));
        final WeakReference<Gui>weakGui=new WeakReference<>(gui);

        gui.setInventoryClickEvent(e2->e2.setCancelled(true));
        gui.setBackground(new GuiItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)));

        gui.setEffectiveCooldownMs(500L);

        //GUI-PAGE
        final GuiPage guiPage=new GuiPage(1,1,7,3);
        gui.addGuiPage("pagination",guiPage);
        guiPage.setBackground(new GuiItem(new ItemStack(Material.WHITE_STAINED_GLASS_PANE)));
        reloadSecondaryItems(weakGui,type,guiPage,p,actualisation);

        //GUI PAGE-ITEM
        gui.addGuiItem(1,4,new GuiItem(ItemBuilder.createItem(Material.ARROW).setDisplayName("←").build(),e2->{
            if(e2.getClick().equals(ClickType.DOUBLE_CLICK))return;
            guiPage.previous();
            guiPage.update();
        }));
        gui.addGuiItem(7,4,new GuiItem(ItemBuilder.createItem(Material.ARROW).setDisplayName("→").build(),e2->{
            if(e2.getClick().equals(ClickType.DOUBLE_CLICK))return;
            guiPage.next();
            guiPage.update();
        }));

        //UPDATE-ITEM
        updateActualisationButton(weakGui,type,p,actualisation,4);
        if(actualisation.get()){
            reloadSecondaryItems(weakGui,type,guiPage,p,actualisation);
            gui.setActiveGuiTickConsumer(UPDATE_INTERVAL,guiRef->{
                if(!guiRef.getInventory().getViewers().isEmpty())reloadSecondaryItems(weakGui,type,guiPage,p,actualisation);
            });
        }

        //BACK
        final ItemBuilder itemBuilder=ItemBuilder.createItem(Material.SPRUCE_DOOR);
        itemBuilder.setDisplayName("§cRetour");
        gui.addGuiItem(4,0,new GuiItem(itemBuilder.build(),e->{
            final GuiPage mainGuiPage=mainGui.getGuiPage("pagination");
            if(mainGuiPage!=null){
                if(actualisation.get()){
                    reloadPrimaryItems(weakMainGui,mainGuiPage,p,actualisation);
                    mainGui.setActiveGuiTickConsumer(UPDATE_INTERVAL,guiRef->{
                        if(!guiRef.getInventory().getViewers().isEmpty())reloadPrimaryItems(weakMainGui,mainGuiPage,p,actualisation);
                    });
                }else mainGui.setActiveGuiTickConsumer(0,null);
            }

            updateActualisationButton(weakMainGui,null,p,actualisation,2);
            mainGui.display(p);
        }));


        //OTHERS
        gui.generalUpdate();
        Bukkit.getScheduler().runTaskLater(Core.getInstance(),()->
                gui.display(p),4L);
    }
    private static void reloadSecondaryItems(@NotNull WeakReference<Gui>weakGui,@NotNull PerformanceTracker.Types type,@NotNull GuiPage guiPage,@NotNull Player p,@NotNull AtomicBoolean actualisation){
        final Gui gui=weakGui.get();
        if(gui==null)return;

        guiPage.clearGuiItems();

        for(final String method:PerformanceTracker.getMethods(type)){
            PerformanceTracker.TrackerData trackerData=PerformanceTracker.get(type,method);
            if(trackerData==null)continue;

            long syncMillis=trackerData.syncTime()/1_000_000;
            int syncSeconds=(int)(trackerData.syncTime()/1_000_000_000L);

            long asyncMillis=trackerData.asyncTime()/1_000_000;
            int asyncSeconds=(int)(trackerData.asyncTime()/1_000_000_000L);

            final ItemBuilder itemBuilder=ItemBuilder.createItem(Material.GRASS_BLOCK);
            itemBuilder.setDisplayName(method);

            setItemBuilderLore(itemBuilder,trackerData.syncCount(),trackerData.syncTime(),syncMillis,syncSeconds,trackerData.asyncCount(),trackerData.asyncTime(),asyncMillis,asyncSeconds);

            guiPage.addGuiItem(new GuiItem(itemBuilder.build()));
        }

        gui.generalUpdate();
    }

    //OTHERS
    private static void updateActualisationButton(@NotNull WeakReference<Gui>weakGui,@Nullable PerformanceTracker.Types type,@NotNull Player p, @NotNull AtomicBoolean actualisation, int y){
        final Gui gui=weakGui.get();
        if(gui==null)return;

        final GuiPage guiPage=gui.getGuiPage("pagination");
        if(guiPage==null)return;

        if(actualisation.get()){
            gui.addGuiItem(4,y,new GuiItem(ItemBuilder.createItem(Material.GREEN_DYE).setDisplayName("Actualisation activée").build(),e2->{
                if(e2.getClick().equals(ClickType.DOUBLE_CLICK))return;

                actualisation.set(false);

                gui.setActiveGuiTickConsumer(0,null);

                updateActualisationButton(weakGui,type,p,actualisation,y);
            }));
        }else{
            gui.addGuiItem(4,y,new GuiItem(ItemBuilder.createItem(Material.RED_DYE).setDisplayName("Actualisation désactivée").build(),e2->{
                if(e2.getClick().equals(ClickType.DOUBLE_CLICK))return;

                actualisation.set(true);

                if(y==2){
                    reloadPrimaryItems(weakGui,guiPage,p,actualisation);
                    gui.setActiveGuiTickConsumer(UPDATE_INTERVAL,guiRef->{
                        if(!guiRef.getInventory().getViewers().isEmpty())reloadPrimaryItems(weakGui,guiPage,p,actualisation);
                        else guiRef.setActiveGuiTickConsumer(0,null);
                    });
                }else if(type!=null){
                    reloadSecondaryItems(weakGui,type,guiPage,p,actualisation);
                    gui.setActiveGuiTickConsumer(UPDATE_INTERVAL,guiRef->{
                        if(!guiRef.getInventory().getViewers().isEmpty())reloadSecondaryItems(weakGui,type,guiPage,p,actualisation);
                        else guiRef.setActiveGuiTickConsumer(0,null);
                    });
                }

                updateActualisationButton(weakGui,type,p,actualisation,y);
            }));
        }

        gui.generalUpdate();
    }



    private static void setItemBuilderLore(@NotNull ItemBuilder itemBuilder,int syncCall,long syncNanos,long syncMillis,int syncSeconds,int asyncCall,long asyncNanos,long asyncMillis,int asyncSeconds){
        final int tSyncIteration=Math.max(1,syncCall);
        final int tAsyncIteration=Math.max(1,asyncCall);
        itemBuilder.setLore(
                "§7Appels: §f"+syncCall+" §7| §e"+asyncCall,
                "",
                "§7Nanos: §f"+syncNanos+" §7| §e"+asyncNanos,
                "§7Moyenne Nanos: §f"+syncNanos/tSyncIteration+" §7| §e"+asyncNanos/tAsyncIteration,
                "",
                "§7Millis: §f"+syncMillis+" §7| §e"+asyncMillis,
                "§7Moyenne Millis: §f"+syncMillis/tSyncIteration+" §7| §e"+asyncMillis/tAsyncIteration,
                "",
                "§7Secondes: §f"+syncSeconds+" §7| §e"+asyncSeconds,
                "§7Moyenne Secondes: §f"+syncSeconds/tSyncIteration+" §7| §e"+asyncSeconds/tAsyncIteration
        );
    }
}