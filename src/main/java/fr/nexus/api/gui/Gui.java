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
import org.bukkit.inventory.InventoryHolder;
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
    protected final int width;
    protected final int height;

    private final@NotNull Inventory inventory;
    private final@Nullable Component title;

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

    public Gui(@Nullable InventoryHolder owner,int rows,@Nullable Component title){
        this.weakReference=new WeakReference<>(this);
        this.width = 9;
        this.height = Math.max(1,Math.min(6,rows));
        this.title = title;

        final int size=this.height*this.width;
        if(title==null)this.inventory=Bukkit.createInventory(owner!=null?owner:new GuiHolder(this),size);
        else this.inventory=Bukkit.createInventory(owner!=null?owner:new GuiHolder(this),size,title);

        GuiManager.addGui(this.inventory,this);

        this.cleanable=Core.getCleaner().register(this,new Unload(this.globalGuiTickConsumer));
    }
    public Gui(int rows,@Nullable Component title){
        this.weakReference=new WeakReference<>(this);
        this.width = 9;
        this.height = Math.max(1,Math.min(6,rows));
        this.title = title;

        final int size=this.height*this.width;
        if(title==null)this.inventory=Bukkit.createInventory(new GuiHolder(this),size);
        else this.inventory=Bukkit.createInventory(new GuiHolder(this),size,title);

        GuiManager.addGui(this.inventory,this);

        this.cleanable=Core.getCleaner().register(this,new Unload(this.globalGuiTickConsumer));
    }

    public Gui(@Nullable InventoryHolder owner,@NotNull InventoryType type,@Nullable String title){
        this(owner,type,title!=null?Component.text(title):null);
    }
    public Gui(@NotNull InventoryType type,@Nullable String title){
        this(type,title!=null?Component.text(title):null);
    }

    public Gui(@Nullable InventoryHolder owner,@NotNull InventoryType type,@Nullable Component title){
        this.weakReference=new WeakReference<>(this);
        this.width = 9; // Valeur par défaut si appelé par l'ancien constructeur
        this.height = Math.max(1, type.getDefaultSize() / 9);
        this.title = title;

        if(title==null)this.inventory=Bukkit.createInventory(owner!=null?owner:new GuiHolder(this),type);
        else this.inventory=Bukkit.createInventory(owner!=null?owner:new GuiHolder(this),type,title);

        GuiManager.addGui(this.inventory,this);

        this.cleanable=Core.getCleaner().register(this,new Unload(this.globalGuiTickConsumer));
    }
    public Gui(@NotNull InventoryType type,@Nullable Component title){
        this.weakReference=new WeakReference<>(this);
        this.width = 9;
        this.height = Math.max(1, type.getDefaultSize() / 9);
        this.title = title;

        if(title==null)this.inventory=Bukkit.createInventory(new GuiHolder(this),type);
        else this.inventory=Bukkit.createInventory(new GuiHolder(this),type,title);

        GuiManager.addGui(this.inventory,this);

        this.cleanable=Core.getCleaner().register(this,new Unload(this.globalGuiTickConsumer));
    }

    // CONSTRUCTEUR PROTECTED POUR TES CLASSES SPECIFIQUES (FURNACE, HOPPER, ETC.)
    protected Gui(@Nullable InventoryHolder owner, @NotNull InventoryType type, @Nullable Component title, int width, int height) {
        this.weakReference = new WeakReference<>(this);
        this.width = width;
        this.height = height;
        this.title = title;

        if (title == null) this.inventory = Bukkit.createInventory(owner!=null?owner:new GuiHolder(this), type);
        else this.inventory = Bukkit.createInventory(owner!=null?owner:new GuiHolder(this), type, title);

        GuiManager.addGui(this.inventory, this);

        this.cleanable = Core.getCleaner().register(this, new Unload(this.globalGuiTickConsumer));
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

    public @Nullable Component getTitle(){
        return this.title;
    }

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
    public void addCooldown(@NotNull UUID uuid,@NotNull Long effectiveCooldownMs){
        this.cooldowns.put(uuid,effectiveCooldownMs);
    }
    public void removeCooldown(@NotNull UUID uuid){
        this.cooldowns.remove(uuid);
    }

    //GUI-ITEMS
    public void addGuiItem(int x,int y,@NotNull GuiItem guiItem){
        addGuiItem(x+y*this.width,guiItem);
    }
    public void addGuiItem(int x,int y,@NotNull ItemStack item){
        addGuiItem(x+y*this.width,new GuiItem(item));
    }
    public void addGuiItem(int x,int y,@NotNull ItemStack item,@Nullable Consumer<@NotNull InventoryClickEvent>action){
        addGuiItem(x+y*this.width,new GuiItem(item,action));
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
        removeGuiItem(x+y*this.width);
    }
    public void removeGuiItem(int slot){
        this.guiItems.remove(slot);
    }

    public@Nullable GuiItem getGuiItemAt(int x,int y){
        return getGuiItemAt(x+y*this.width);
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
        guiPage.setGuiWidth(this.width); // Transmission de la largeur dynamique
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
        guiSlider.setGuiWidth(this.width); // Transmission de la largeur dynamique
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
    public void display(@NotNull Player... players) {
        for (final Player p : players) {
            if (!p.isOnline()) continue;

            // On force l'exécution sur le scheduler propre de l'entité du joueur
            p.getScheduler().run(Core.getInstance(), task -> {
                p.openInventory(this.inventory);
            }, null);
        }
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

    public void destroy() {
        // Annulation des tâches
        if (this.globalGuiTickConsumer != null && this.globalGuiTickConsumer.getTask() != null) {
            this.globalGuiTickConsumer.getTask().cancel();
        }
        if (this.activeGuiTickConsumer != null && this.activeGuiTickConsumer.getTask() != null) {
            this.activeGuiTickConsumer.getTask().cancel();
        }

        // Effacement des callbacks qui capturent des références externes
        this.globalClickEvent = null;
        this.inventoryClickEvent = null;
        this.dragEvent = null;
        this.closeEvent = null;

        // Purge des items et des panneaux
        this.guiItems.clear();
        this.guiPages.clear();
        this.guiSliders.clear();
        this.cooldowns.clear();
    }

    //INNER CLASS
    private record Unload(@Nullable GuiConsumer consumer)implements Runnable{
        @Override
        public void run(){
            if(consumer!=null&&consumer.getTask()!=null)consumer.getTask().cancel();
        }
    }
}