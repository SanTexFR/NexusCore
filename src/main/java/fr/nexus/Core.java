package fr.nexus;

import fr.nexus.listeners.core.CoreDisableEvent;
import fr.nexus.listeners.core.CoreInitializeEvent;
import fr.nexus.listeners.core.CoreReloadEvent;
import fr.nexus.listeners.Listeners;
import fr.nexus.logger.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Cleaner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class Core extends JavaPlugin{
    //VARIABLES (STATICS)
    private static Logger logger;
    private static final int amount=1_000_000;
    public static long CLEANUP_INTERVAL;

    //VARIABLES (STATICS)
    private static Core instance;
    private static Listeners listeners;
    private static final@NotNull Cleaner cleaner=Cleaner.create();

    private static final@NotNull UUID sessionUUID=UUID.randomUUID();

    //DANS VAR AJOUTER UNE VERIF POUR LES SAVESYNC ET LOAD SYNC, POUR LE PAS POUVOIR LES LANCEES EN ASYNC

    //METHODS (OVERRIDE)
    @Override
    public void onEnable(){
//        org.spigotmc.AsyncCatcher.enabled = false;

        //INSTANCE
        instance=this;
        logger=new Logger(this,Core.class);

        //CONFIG
        saveDefaultConfig();

        //RELOAD
        reload();

        //LISTENERS
        final Listeners listener=new Listeners();
        listeners=listener;
        getServer().getPluginManager().registerEvents(listener,this);

        //INITIALIZER
        ClazzInitializer.initialize();

        //INITIALIZE
        Bukkit.getPluginManager().callEvent(new CoreInitializeEvent());
        Listeners.register(CoreReloadEvent.class,Listeners::onCoreReload);

        //TEST
        //Bukkit.getScheduler().runTaskLater(getInstance(),Core::testFile,40L);
        //Listeners.register(PlayerJoinEvent.class,Core::onPlayerJoin);
    }

    @Override
    public void onDisable(){
        Bukkit.getPluginManager().callEvent(new CoreDisableEvent());
        shutdownExecutor(Listeners.THREADPOOL);
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

    public static void reload(boolean safe){
        getInstance().reloadConfig();

        reload();

        if(Bukkit.isPrimaryThread())Bukkit.getPluginManager().callEvent(new CoreReloadEvent(safe));
        else Bukkit.getScheduler().runTask(Core.getInstance(),()->
            Bukkit.getPluginManager().callEvent(new CoreReloadEvent(safe)));

    }
    private static void reload(){
        CLEANUP_INTERVAL=getInstance().getConfig().getLong("cache.cleanupInterval",6000);
    }


//    @TestOnly
//    public static void onPlayerJoin(PlayerJoinEvent e){
//        final Player p=e.getPlayer();
//
//
//        final ItemStack item=Utils.getHeadByTexture(
//                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q5NTg1MzVmMjY0ZjE2Mzk4YjI4OTY0MGMwYzUzOTFhNDE4NzlkMTk2NzMxYTcxNjY0N2QzYzY0NjZjZGNkZSJ9fX0="
//                );
//
//
//
//
//
//
//        final Gui gui=new Gui(6,Component.text("salam rhoya"));
//
//
//
//
//
//
//
//
//
//        gui.setGlobalClickEvent(e2->
//            e2.setCancelled(true));
//
//        gui.setBackground(new GuiItem(ItemBuilder.createItem(Material.BLACK_STAINED_GLASS_PANE).setDisplayName("salut").build(),e2->{
//            if(e2.getClick().equals(ClickType.DOUBLE_CLICK))return;
//            System.out.println("background: "+e2.getRawSlot());
//        }));
//
////        final GuiPage guiPanel=new GuiPage(0,0);
////        gui.addGuiPage("pagination",guiPanel);
////        guiPanel.setRecursive(true);
//        final GuiPanel guiPanel=new GuiPage(0,0);
//        if(guiPanel instanceof GuiPage guiPage){
//            gui.addGuiPage("pagination",guiPage);
//        }else if(guiPanel instanceof GuiSlider guiSlider){
//            gui.addGuiSlider("pagination",guiSlider);
//        }
//
//        guiPanel.setBackground(new GuiItem(ItemBuilder.createItem(Material.WHITE_STAINED_GLASS_PANE).build(),e2->{
//            if(e2.getClick().equals(ClickType.DOUBLE_CLICK))return;
//            System.out.println("pageBackground");
//        }));
//
//        for (int i = 0; i < 21; i++) {
//            final int count=i;
//            guiPanel.addGuiItem(new GuiItem(ItemBuilder.createItem(ItemBuilder.getRandomMaterial()).setDisplayName(""+count).build(),e2->{
//                if(e2.getClick().equals(ClickType.DOUBLE_CLICK))return;
//                System.out.println("customItemSlot: "+count);
//            }));
//        }
//        guiPanel.addGuiItemAtIndex(5,new GuiItem(ItemBuilder.createItem(ItemBuilder.getRandomMaterial()).setDisplayName("INTRUUUUUUU").build(),e2->{
//            if(e2.getClick().equals(ClickType.DOUBLE_CLICK))return;
//            System.out.println("INTRUUUUUUU");
//        }));
//
//        Bukkit.getScheduler().runTaskLater(getInstance(),()->{
//            gui.display(p);
//            p.setGameMode(GameMode.CREATIVE);
//        },50L);
//
//        //ARROWS
//
//        gui.addGuiItem(6,5,new GuiItem(ItemBuilder.createItem(Material.ARROW).setDisplayName("-").build(),e2->{
//            if(e2.getClick().equals(ClickType.DOUBLE_CLICK))return;
//            guiPanel.previous();
//            guiPanel.update();
//        }));
//        gui.addGuiItem(8,5,new GuiItem(ItemBuilder.createItem(Material.ARROW).setDisplayName("+").build(),e2->{
//            if(e2.getClick().equals(ClickType.DOUBLE_CLICK))return;
//            guiPanel.next();
//            guiPanel.update();
//        }));
//
//
//
//        gui.addGuiItem(7,3,new GuiItem(ItemBuilder.createItem(Material.PAPER).build(),e2->{
//            if(e2.getClick().equals(ClickType.DOUBLE_CLICK))return;
//            if(guiPanel.getPanelSecondSlot()<9)return;
//            guiPanel.defineSlots(guiPanel.getPanelFirstSlot(),guiPanel.getPanelSecondSlot()-9);
//            gui.generalUpdate();
//        }));
//        gui.addGuiItem(7,5,new GuiItem(ItemBuilder.createItem(Material.PAPER).build(),e2->{
//            if(e2.getClick().equals(ClickType.DOUBLE_CLICK))return;
//            if(guiPanel.getPanelSecondSlot()+9>53)return;
//            guiPanel.defineSlots(guiPanel.getPanelFirstSlot(),guiPanel.getPanelSecondSlot()+9);
//            guiPanel.update();
//        }));
//
//        gui.addGuiItem(6,4,new GuiItem(ItemBuilder.createItem(Material.PAPER).build(),e2->{
//            if(e2.getClick().equals(ClickType.DOUBLE_CLICK))return;
//            if(guiPanel.getPanelSecondSlot()<=0)return;
//
//            int rowStart = (guiPanel.getPanelSecondSlot() / 9) * 9; // début de la ligne actuelle
//            guiPanel.defineSlots(guiPanel.getPanelFirstSlot(), Math.max(guiPanel.getPanelSecondSlot() - 1, rowStart));
//            gui.generalUpdate();
//        }));
//        gui.addGuiItem(8,4,new GuiItem(ItemBuilder.createItem(Material.PAPER).build(),e2->{
//            if(e2.getClick().equals(ClickType.DOUBLE_CLICK))return;
//            int rowEnd = ((guiPanel.getPanelSecondSlot() / 9) + 1) * 9 - 1;
//            guiPanel.defineSlots(guiPanel.getPanelFirstSlot(),Math.min(guiPanel.getPanelSecondSlot()+1,rowEnd));
//            guiPanel.update();
//        }));
//
//        gui.addGuiItem(7,4,new GuiItem(ItemBuilder.createItem(Material.SUNFLOWER).build(),e2->{
//            if(e2.getClick().equals(ClickType.DOUBLE_CLICK))return;
//            gui.generalUpdate();
//        }));
//
//        //RECURSIVE
//
//        if(guiPanel instanceof GuiSlider guiSlider){
//            gui.addGuiItem(0,5,new GuiItem(ItemBuilder.createItem(Material.RED_DYE).setDisplayName("Horizontal").build(),e2->{
//                if(e2.getClick().equals(ClickType.DOUBLE_CLICK))return;
//                guiSlider.setHorizontal();
//                gui.generalUpdate();
//            }));
//            gui.addGuiItem(1,5,new GuiItem(ItemBuilder.createItem(Material.LIME_DYE).setDisplayName("Vertical").build(),e2->{
//                if(e2.getClick().equals(ClickType.DOUBLE_CLICK))return;
//                guiSlider.setVertical();
//                gui.generalUpdate();
//            }));
//
//            gui.addGuiItem(3,5,new GuiItem(ItemBuilder.createItem(Material.RED_DYE).setDisplayName("Non recursive").build(),e2->{
//                if(e2.getClick().equals(ClickType.DOUBLE_CLICK))return;
//                guiPanel.setRecursive(false);
//                gui.generalUpdate();
//            }));
//            gui.addGuiItem(4,5,new GuiItem(ItemBuilder.createItem(Material.LIME_DYE).setDisplayName("Recursive").build(),e2->{
//                if(e2.getClick().equals(ClickType.DOUBLE_CLICK))return;
//                guiPanel.setRecursive(true);
//                gui.generalUpdate();
//            }));
//        }else{
//            gui.addGuiItem(0,5,new GuiItem(ItemBuilder.createItem(Material.RED_DYE).setDisplayName("Non recursive").build(),e2->{
//                if(e2.getClick().equals(ClickType.DOUBLE_CLICK))return;
//                guiPanel.setRecursive(false);
//                gui.generalUpdate();
//            }));
//            gui.addGuiItem(1,5,new GuiItem(ItemBuilder.createItem(Material.LIME_DYE).setDisplayName("Recursive").build(),e2->{
//                if(e2.getClick().equals(ClickType.DOUBLE_CLICK))return;
//                guiPanel.setRecursive(true);
//                gui.generalUpdate();
//            }));
//        }
//
//        gui.generalUpdate();
//    }
//
//
//    @TestOnly
//    public static void testFile(){
//        System.out.println();
//        System.out.println("---- SYNC FILE ----");
//
//        long time=System.currentTimeMillis();
//        final VarFile var=VarFile.getVarSync(getInstance(),"test");
//        System.out.println("Load time: "+(System.currentTimeMillis()-time)+"ms");
//        System.out.println("Amount: "+var.getKeys().size());
//
//        final UUID uuid=UUID.randomUUID();
//        time=System.currentTimeMillis();
//        for (int i = 0; i < amount; i++) {
//            var.setValue(VarTypes.UUID,"test"+i,UUID.randomUUID());
//        }
//        System.out.println("Set time: "+(System.currentTimeMillis()-time)+"ms");
//
//        time=System.currentTimeMillis();
//        for (int i = 0; i < amount; i++) {
//            var.getValue(VarTypes.UUID,"test"+i);
//        }
//        System.out.println("Get time: "+(System.currentTimeMillis()-time)+"ms");
//
//        time=System.currentTimeMillis();
//        var.saveSync();
//        System.out.println("Save time: "+(System.currentTimeMillis()-time)+"ms");
//
//        time=System.currentTimeMillis();
//        var.unload();
//        System.out.println("Unload time: "+(System.currentTimeMillis()-time)+"ms");
//
//        Bukkit.getScheduler().runTaskLater(getInstance(),Core::testFile2,40L);
//    }
//
//    @TestOnly
//    public static void testFile2(){
//        System.out.println();
//        System.out.println("---- ASYNC FILE ----");
//
//        final AtomicLong time=new AtomicLong(System.currentTimeMillis());
//        VarFile.getVarAsync(getInstance(),"test").thenAcceptAsync(var->{
//            System.out.println("Load time: "+(System.currentTimeMillis()-time.get())+"ms");
//            System.out.println("Amount: "+var.getKeys().size());
//
//            final UUID uuid=UUID.randomUUID();
//            long time2=System.currentTimeMillis();
//            for (int i = 0; i < amount; i++) {
//                var.setValue(VarTypes.UUID,"test"+i,UUID.randomUUID());
//            }
//            System.out.println("Set time: "+(System.currentTimeMillis()-time2)+"ms");
//
//            time2=System.currentTimeMillis();
//            for (int i = 0; i < amount; i++) {
//                var.getValue(VarTypes.UUID,"test"+i);
//            }
//            System.out.println("Get time: "+(System.currentTimeMillis()-time2)+"ms");
//
//            final AtomicLong time3=new AtomicLong(System.currentTimeMillis());
//            var.saveAsync().thenAccept(v->{
//                System.out.println("Save time: "+(System.currentTimeMillis()-time3.get())+"ms");
//                testSql();
//            });
//        });
//    }
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//    @TestOnly
//    public static void testSql(){
//        System.out.println();
//        System.out.println("---- SYNC SQL ----");
//
//        long time=System.currentTimeMillis();
//        final VarSql var=VarSql.getVarSync("main","players","test");
//        System.out.println("Load time: "+(System.currentTimeMillis()-time)+"ms");
//        System.out.println("Amount: "+var.getKeys().size());
//
//        final UUID uuid=UUID.randomUUID();
//        time=System.currentTimeMillis();
//        for (int i = 0; i < amount; i++) {
//            var.setValue(VarTypes.UUID,"test"+i,UUID.randomUUID());
//        }
//        System.out.println("Set time: "+(System.currentTimeMillis()-time)+"ms");
//
//        time=System.currentTimeMillis();
//        for (int i = 0; i < amount; i++) {
//            var.getValue(VarTypes.UUID,"test"+i);
//        }
//        System.out.println("Get time: "+(System.currentTimeMillis()-time)+"ms");
//
//        time=System.currentTimeMillis();
//        var.saveSync();
//        System.out.println("Save time: "+(System.currentTimeMillis()-time)+"ms");
//
//        time=System.currentTimeMillis();
//        var.unload();
//        System.out.println("Unload time: "+(System.currentTimeMillis()-time)+"ms");
//
//        Bukkit.getScheduler().runTaskLater(getInstance(),Core::testSql2,40L);
//    }
//
//    @TestOnly
//    public static void testSql2(){
//        System.out.println();
//        System.out.println("---- ASYNC SQL ----");
//
//        final AtomicLong time=new AtomicLong(System.currentTimeMillis());
//        VarSql.getVarAsync("main","players","test").thenAccept(var->{
//            System.out.println("Load time: "+(System.currentTimeMillis()-time.get())+"ms");
//            System.out.println("Amount: "+var.getKeys().size());
//
//            final UUID uuid=UUID.randomUUID();
//            long time2=System.currentTimeMillis();
//            for (int i = 0; i < amount; i++) {
//                var.setValue(VarTypes.UUID,"test"+i,UUID.randomUUID());
//            }
//            System.out.println("Set time: "+(System.currentTimeMillis()-time2)+"ms");
//
//            time2=System.currentTimeMillis();
//            for (int i = 0; i < amount; i++) {
//                var.getValue(VarTypes.UUID,"test"+i);
//            }
//            System.out.println("Get time: "+(System.currentTimeMillis()-time2)+"ms");
//
//            final AtomicLong time3=new AtomicLong(System.currentTimeMillis());
//            var.saveAsync().thenAccept(v->
//                System.out.println("Save time: "+(System.currentTimeMillis()-time3.get())+"ms"));
//        });
//    }


    //LOGGER  🟠 NEED REWORK

    //LANGUAGE 🔴 NEED REWORK

    //VAR ([FLAT-FILE AND SQL] IN-RAM STORAGE) #WITH DOCUMENTATION ✔️
    //VAR-OBJECT #WITH DOCUMENTATION ✔️

    //DYNAMIC LISTENERS #WITH DOCUMENTATION ✔️

    //GUI 🟠 USE FAST-UTILS MAP

    //PERFORMANCE TRACKER ✔️

    //ACTIONBAR 🔴

    //ITEM BUILDER 🟠

    //COMMAND ✔️

    //TAB-COMPLETER ✔️



    //https://www.spigotmc.org/resources/imageryapi-take-pictures-in-minecraft.111347/
    //ImageryAPI - Take pictures in Minecraft 1.2.2

    //https://www.spigotmc.org/resources/%E2%9C%A8-xray-no-mods-no-resourcepack-no-entities.95178/
    //✨ Xray | No mods, no resourcepack, no entities 1.1.0

    //https://www.spigotmc.org/resources/advancedcore.28295/
    //AdvancedCore 3.7.16
}