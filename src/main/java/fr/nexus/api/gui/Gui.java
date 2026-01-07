package fr.nexus.api.gui;

import com.cjcrafter.foliascheduler.TaskImplementation;
import fr.nexus.Core;
import fr.nexus.api.gui.modules.GuiBackground;
import fr.nexus.api.gui.panels.GuiPage;
import fr.nexus.api.gui.panels.GuiPanel;
import fr.nexus.api.gui.panels.GuiSlider;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class Gui implements GuiBackground{
    //VARIABLES (INSTANCES)
    private final@NotNull Inventory inventory;

    private final@NotNull ConcurrentHashMap<@NotNull UUID,@NotNull Long>cooldowns=new ConcurrentHashMap<>();
    private@Nullable Long effectiveCooldownMs;

    private@Nullable GuiItem background;

    private@Nullable GuiReuse reuse;

    private final@NotNull WeakReference<Gui>weakReference;

    //ITEMS
    private final@NotNull ConcurrentHashMap<@NotNull Integer,@NotNull GuiItem>guiItems=new ConcurrentHashMap<>();
    private final@NotNull ConcurrentHashMap<@NotNull String,@NotNull GuiPage>guiPages=new ConcurrentHashMap<>();
    private final@NotNull ConcurrentHashMap<@NotNull String,@NotNull GuiSlider>guiSliders=new ConcurrentHashMap<>();

    //EVENTS
    private@Nullable Consumer<@NotNull InventoryClickEvent>globalClickEvent,inventoryClickEvent;
    private@Nullable Consumer<@NotNull InventoryDragEvent>dragEvent;
    private@Nullable Consumer<@NotNull InventoryCloseEvent>closeEvent;

    //RUNNABLE
    private@Nullable GuiConsumer activeGuiTickConsumer,globalGuiTickConsumer;
    private final@NotNull Cleaner.Cleanable cleanable;

    //CONSTRUCTOR
    public Gui(int rows,@Nullable String title){
        this(rows,title!=null?Component.text(title):null);
    }
    public Gui(@NotNull GuiReuse reuse,int rows,@Nullable String title){
        this(rows,title!=null?Component.text(title):null);
    }

    public Gui(int rows,@Nullable Component title){
        this.weakReference=new WeakReference<>(this);

        final int size=Math.max(1,Math.min(6,rows))*9;
        if(title==null)this.inventory=Bukkit.createInventory(null,size);
        else this.inventory=Bukkit.createInventory(null,size,title);

        GuiManager.addGui(this.inventory,this);

        this.cleanable=Core.getCleaner().register(this,new Unload(this.globalGuiTickConsumer));
    }

    public Gui(@NotNull InventoryType type,@Nullable String title){
        this(type,title!=null?Component.text(title):null);
    }
    public Gui(@NotNull InventoryType type,@Nullable Component title){
        this.weakReference=new WeakReference<>(this);

        if(title==null)this.inventory=Bukkit.createInventory(null,type);
        else this.inventory=Bukkit.createInventory(null,type,title);

        GuiManager.addGui(this.inventory,this);

        this.cleanable=Core.getCleaner().register(this,new Unload(this.globalGuiTickConsumer));
    }


    //METHODS(STATICS)
    public static@NotNull CompletableFuture<@Nullable Gui>getIfCached(@NotNull GuiReuse reuse){

        final Gui gui=GuiManager.reuseGuis.get(reuse.key());

        if(gui==null||gui.getReuse()==null)
            return CompletableFuture.completedFuture(null);

        return gui.getReuse()
                .supplier()
                .get()
                .thenApply(valid->valid?gui:null);
    }


    //METHODS (INSTANCES)

    //WEAK-REFERENCE
    public@NotNull WeakReference<Gui>getWeakReference(){
        return this.weakReference;
    }

    //INVENTORY
    public@NotNull Inventory getInventory(){
        return this.inventory;
    }

    //REUSE
    public void setReuse(@NotNull GuiReuse reuse){
        if(this.reuse!=null)GuiManager.reuseGuis.get(this.reuse.key());

        this.reuse=reuse;

        GuiManager.reuseGuis.put(reuse.key(),this);
    }
    public@Nullable GuiReuse getReuse(){
        return this.reuse;
    }

    //COOLDOWN
    public void setEffectiveCooldownMs(@Nullable Long time){
        if(time!=null)time=Math.max(1,time);
        this.effectiveCooldownMs=time;
    }
    public@Nullable Long getEffectiveCooldown(){
        return this.effectiveCooldownMs;
    }

    public@NotNull ConcurrentHashMap<@NotNull UUID,@NotNull Long>getCooldowns(){
        return this.cooldowns;
    }

    //GUI-ITEMS
    public void addGuiItem(int x,int y,@NotNull GuiItem guiItem){
        addGuiItem(x+y*9,guiItem);
    }
    public void addGuiItem(int x,int y,@NotNull ItemStack item){
        addGuiItem(x+y*9,new GuiItem(item));
    }
    public void addGuiItem(int x,int y,@NotNull ItemStack item,@Nullable Consumer<@NotNull InventoryClickEvent>action){
        addGuiItem(x+y*9,new GuiItem(item,action));
    }

    public void addGuiItem(int slot,@NotNull GuiItem guiItem){
        this.guiItems.put(slot,guiItem);
    }
    public void addGuiItem(int slot,@NotNull ItemStack item){
        addGuiItem(slot,new GuiItem(item));
    }
    public void addGuiItem(int slot,@NotNull ItemStack item,@Nullable Consumer<@NotNull InventoryClickEvent>action){
        addGuiItem(slot,new GuiItem(item,action));
    }

    public void removeGuiItem(int x,int y){
        removeGuiItem(x+y*9);
    }
    public void removeGuiItem(int slot){
        this.guiItems.remove(slot);
    }

    public@Nullable GuiItem getGuiItemAt(int x,int y){
        return getGuiItemAt(x+y*9);
    }
    public@Nullable GuiItem getGuiItemAt(int slot){
        if(slot<0||slot>=this.inventory.getSize())return null;

        final GuiItem guiItem=this.guiItems.get(slot);
        if(guiItem!=null)return this.guiItems.get(slot);

        if(this.background==null)return null;

        for(final GuiPage guiPage:this.guiPages.values())
            if(guiPage.isWithin(slot))return null;
        for(final GuiSlider guiSlider:this.guiSliders.values())
            if(guiSlider.isWithin(slot))return null;

        return this.background;
    }

    //ACTION-GUI RUNNABLE
    public void setGlobalGuiTickConsumer(int tick,@DoNotStoreGui@Nullable Consumer<@NotNull Gui>consumer){
        if(this.globalGuiTickConsumer!=null){
            if(this.globalGuiTickConsumer.getTask()!=null){
                this.globalGuiTickConsumer.getTask().cancel();
                this.globalGuiTickConsumer.setTask(null);
            }this.globalGuiTickConsumer=null;
        }

        if(consumer==null)return;

        tick=Math.max(1,tick);

        final WeakReference<Gui>weakGui=getWeakReference();
        final TaskImplementation<?>task=Core.getServerImplementation().global().runAtFixedRate(()->{
            final Gui gui=weakGui.get();
            if(gui!=null)consumer.accept(gui);
        },tick,tick);

        this.globalGuiTickConsumer=new GuiConsumer(weakGui,tick,consumer,task);
    }
    @Nullable GuiConsumer getGlobalGuiTickConsumer(){
        return this.globalGuiTickConsumer;
    }

    public void setActiveGuiTickConsumer(int tick,@DoNotStoreGui@Nullable Consumer<@NotNull Gui>consumer){
        if(this.activeGuiTickConsumer!=null){
            if(this.activeGuiTickConsumer.getTask()!=null){
                this.activeGuiTickConsumer.getTask().cancel();
                this.activeGuiTickConsumer.setTask(null);
            }this.activeGuiTickConsumer=null;
        }

        if(consumer==null)return;

        tick=Math.max(1,tick);

        final WeakReference<Gui>weakGui=getWeakReference();
        final TaskImplementation<?>task=this.inventory.getViewers().isEmpty()?null:Core.getServerImplementation().global().runAtFixedRate(()->{
            final Gui gui=weakGui.get();
            if(gui!=null)consumer.accept(gui);
        },tick,tick);

        this.activeGuiTickConsumer=new GuiConsumer(weakGui,tick,consumer,task);
    }
    @Nullable GuiConsumer getActiveGuiTickConsumer(){
        return this.activeGuiTickConsumer;
    }

    //PANELS

    //GUI-PAGES
    public void removeGuiPage(@NotNull String id){
        final GuiPage guiPage=this.guiPages.remove(id);
        if(guiPage!=null)guiPage.setInventory(null);
    }

    public void addGuiPage(@NotNull String id,@NotNull GuiPage guiPage){
        this.guiPages.put(id,guiPage);
        guiPage.setInventory(this.inventory);
    }
    public@Nullable GuiPage getGuiPage(@NotNull String id){
        return this.guiPages.get(id);
    }
    public@NotNull Map<@NotNull String,@NotNull GuiPage>getGuiPages(){
        return Collections.unmodifiableMap(this.guiPages);
    }

    //GUI-SLIDERS
    public void removeGuiSlider(@NotNull String id){
        final GuiSlider guiSlider=this.guiSliders.remove(id);
        if(guiSlider!=null)guiSlider.setInventory(null);
    }

    public void addGuiSlider(@NotNull String id,@NotNull GuiSlider guiSlider){
        this.guiSliders.put(id,guiSlider);
        guiSlider.setInventory(this.inventory);
    }
    public@Nullable GuiSlider getGuiSlider(@NotNull String id){
        return this.guiSliders.get(id);
    }
    public@NotNull Map<@NotNull String,@NotNull GuiSlider>getGuiSliders(){
        return Collections.unmodifiableMap(this.guiSliders);
    }

    //BACKGROUND
    public@Nullable GuiItem getBackground(){
        return this.background;
    }
    public void setBackground(@Nullable GuiItem guiItem){
        this.background=guiItem;
    }

    //DISPLAY
    public void display(@NotNull Player...players){
        for(final Player p:players)
            p.openInventory(this.inventory);
    }

    //EVENT
    public void setInventoryClickEvent(@Nullable Consumer<@NotNull InventoryClickEvent>globalClickEvent){
        this.inventoryClickEvent=globalClickEvent;
    }
    public@Nullable Consumer<@NotNull InventoryClickEvent>getInventoryClickEvent(){
        return this.inventoryClickEvent;
    }

    public void setGlobalClickEvent(@Nullable Consumer<@NotNull InventoryClickEvent>globalClickEvent){
        this.globalClickEvent=globalClickEvent;
    }
    public@Nullable Consumer<@NotNull InventoryClickEvent>getGlobalClickEvent(){
        return this.globalClickEvent;
    }

    public void setDragEvent(@Nullable Consumer<@NotNull InventoryDragEvent>dragEvent){
        this.dragEvent=dragEvent;
    }
    public@Nullable Consumer<@NotNull InventoryDragEvent>getDragEvent(){
        return this.dragEvent;
    }

    public void setCloseEvent(@Nullable Consumer<@NotNull InventoryCloseEvent>closeEvent){
        this.closeEvent=closeEvent;
    }
    public@Nullable Consumer<@NotNull InventoryCloseEvent>getCloseEvent(){
        return this.closeEvent;
    }

    //UPDATE
    public void update(){
        if(this.background!=null){
            final Set<Integer>slots=this.guiItems.keySet();
            for(int i=0;i<this.inventory.getSize();i++)
                if(!slots.contains(i))this.inventory.setItem(i,this.background.getItem());
        }

        this.guiItems.forEach((slot,guiItem)->{
            if(!guiItem.getItem().getType().equals(Material.AIR))this.inventory.setItem(slot,guiItem.getItem());
        });
    }
    public void generalUpdate(){
        update();
        pagesUpdate();
        slidersUpdate();
    }

    public void pagesUpdate(){
        this.guiPages.values().forEach(GuiPanel::update);
    }
    public void slidersUpdate(){
        this.guiSliders.values().forEach(GuiPanel::update);
    }

    //INNER CLASS
    private record Unload(@Nullable GuiConsumer consumer)implements Runnable{
        @Override
        public void run(){
            if(consumer!=null&&consumer.getTask()!=null)consumer.getTask().cancel();
        }
    }
}