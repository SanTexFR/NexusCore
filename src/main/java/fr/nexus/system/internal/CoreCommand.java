package fr.nexus.system.internal;

import fr.nexus.Core;
import fr.nexus.api.command.CommandCreator;
import fr.nexus.api.command.tabcompleter.TabCompleterHandler;
import fr.nexus.api.gui.GuiManager;
import fr.nexus.api.listeners.Listeners;
import fr.nexus.api.listeners.core.CoreCleanupEvent;
import fr.nexus.api.listeners.core.CoreInitializeEvent;
import fr.nexus.api.var.varObjects.VarObjectBackend;
import fr.nexus.system.internal.information.InformationGui;
import fr.nexus.system.internal.performanceTracker.PerformanceTrackerGui;
import fr.nexus.system.Updater;
import fr.nexus.utils.Utils;
import fr.nexus.api.var.Var;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class CoreCommand {
    //VARIABLES(STATICS)
    static{
        Listeners.register(CoreInitializeEvent.class, CoreCommand::onCoreInitializeEvent);
    }

    //METHODS(STATICS)
    private static void onCoreInitializeEvent(CoreInitializeEvent e){
        CommandCreator.create("core")
                .setPermission("core.commands")
                .setAction((handler,args)->handler
                        .ifNotPlayer(c->{
                            if(args.length<1){
                                c.sendMessage("§cVeuillez indiquez un argument valide. (/core <config, cachesize, version, mesh, cleanup>)");
                                return;
                            }

                            switch(args[0].toLowerCase()){
                                case"config"->{
                                    if(args.length<2||!args[1].equalsIgnoreCase("reload")){
                                        c.sendMessage("§cVeuillez indiquez un argument valide. (/core config <reload> <all, key> <safe, nosafe>)");
                                        return;
                                    }

                                    reloadConfiguration(c,args);
                                }case"cachesize"->{
                                    if(args.length<2||!args[1].equalsIgnoreCase("listeners")&&!args[1].equalsIgnoreCase("var")&&!args[1].equalsIgnoreCase("gui")&&!args[1].equalsIgnoreCase("utils")){
                                        c.sendMessage("§cVeuillez indiquez un argument valide. (/core cachesize <listeners, var, gui, utils> [normal, advanced])");
                                        return;
                                    }

                                    String mode = args.length > 2 ? args[2] : "normal";
                                    cacheSize(c,args[1], mode);
                                }case"mesh"->meshHandler(c,args);
                                case"version"->version(c);
                                case"cleanup"->cleanup(c);
                                default->c.sendMessage("§cCommande incorrecte. (/core <config, cachesize, version, mesh, cleanup>)");
                            }
                        })
                        .ifPlayer(p->{
                            if(args.length<1){
                                p.sendMessage("§cVeuillez indiquez un argument valide. (/core <config, performance, cachesize, information, version, mesh, cleanup>)");
                                return;
                            }

                            switch(args[0].toLowerCase()){
                                case"config"->{
                                    if(args.length<2||!args[1].equalsIgnoreCase("reload")){
                                        p.sendMessage("§cVeuillez indiquez un argument valide. (/core config <reload> <safe, nosafe>)");
                                        return;
                                    }

                                    reloadConfiguration(p,args);
                                }case"performance"->{
                                    if(args.length<2||!args[1].equalsIgnoreCase("gui")){
                                        p.sendMessage("§cVeuillez indiquez un argument valide. (/core performance <gui>)");
                                        return;
                                    }

                                    PerformanceTrackerGui.primaryGui(p);
                                }case"cachesize"->{
                                    if(args.length<2||!args[1].equalsIgnoreCase("listeners")&&!args[1].equalsIgnoreCase("var")&&!args[1].equalsIgnoreCase("gui")&&!args[1].equalsIgnoreCase("utils")){
                                        p.sendMessage("§cVeuillez indiquez un argument valide. (/core cachesize <listeners, var, gui, utils> [normal, advanced])");
                                        return;
                                    }

                                    String mode = args.length > 2 ? args[2] : "normal";
                                    cacheSize(p,args[1], mode);
                                }case"mesh"->meshHandler(p,args);
                                case"information"->
                                        InformationGui.primaryGui(p);
                                case"version"->version(p);
                                case"cleanup"->cleanup(p);
                                default->p.sendMessage("§cCommande incorrecte. (/core <performance, config, cachesize, version, mesh, cleanup>)");
                            }
                        })
                ).perform();

        TabCompleterHandler.create("core").addDisplay(sender->
                ()->Set.of("performance","config","cachesize","version","information","mesh","cleanup")).perform();

        TabCompleterHandler.create("core").addArg(sender->()->"config").addDisplay(sender->
                ()->Set.of("reload")).perform();
        TabCompleterHandler.create("core").addArg(sender->()->"config").addArg(sender->()->"reload").addDisplay(sender->
                ()->Set.of("all","<key>")).perform();
        TabCompleterHandler.create("core").addArg(sender->()->"config").addArg(sender->()->"reload").addArg(null).addDisplay(sender->
                ()->Set.of("safe","nosafe")).perform();
        TabCompleterHandler.create("core").addArg(sender->()->"mesh").addDisplay(sender->
                ()->Set.of("save")).perform();
        TabCompleterHandler.create("core").addArg(sender->()->"performance").addDisplay(sender->
                ()->Set.of("gui")).perform();

        // MODIFICATION DES TAB COMPLETERS POUR LE CACHESIZE AVANCÉ
        TabCompleterHandler.create("core").addArg(sender->()->"cachesize").addDisplay(sender->
                ()->Set.of("listeners","var","gui","utils")).perform();
        TabCompleterHandler.create("core").addArg(sender->()->"cachesize").addArg(sender->()->"gui").addDisplay(sender->
                ()->Set.of("normal","advanced")).perform();
        TabCompleterHandler.create("core").addArg(sender->()->"cachesize").addArg(sender->()->"var").addDisplay(sender->
                ()->Set.of("normal","advanced")).perform();
    }

    private static void reloadConfiguration(@NotNull CommandSender sender,@NotNull String[]args){
        String key=args.length>2?args[2]:null;
        boolean nosafe=(args.length>3&&args[3].equalsIgnoreCase("nosafe"));

        final long time=System.currentTimeMillis();
        Core.reload(key!=null&&key.equalsIgnoreCase("all")?null:key,nosafe);

        if(nosafe)sender.sendMessage("§eConfiguration non sécurisée rechargée en "+(System.currentTimeMillis()-time)+"ms !");
        else sender.sendMessage("§eConfiguration sécurisée rechargée en "+(System.currentTimeMillis()-time)+"ms !");
    }

    private static void cacheSize(@NotNull CommandSender s, @NotNull String arg, @NotNull String mode) {
        boolean isAdvanced = mode.equalsIgnoreCase("advanced");

        switch (arg.toLowerCase()) {
            case "listeners" -> {
                //SYNC
                s.sendMessage("§e - SyncTypeAmount: " + Listeners.syncEventsRegistered.size());

                final int[] syncAmount = {0};
                Listeners.syncEventsRegistered.forEach((key, value) -> syncAmount[0] += value.size());
                s.sendMessage("§e - SyncAmount: " + syncAmount[0]);

                //ASYNC
                s.sendMessage("§e - AsyncTypeAmount: " + Listeners.asyncEventsRegistered.size());

                final int[] asyncAmount = {0};
                Listeners.asyncEventsRegistered.forEach((key, value) -> asyncAmount[0] += value.size());
                s.sendMessage("§e - SyncAmount: " + asyncAmount[0]);
            }
            case "var" -> {
                s.sendMessage("§e=== Cache VAR (" + (isAdvanced ? "Avancé" : "Normal") + ") ===");
                s.sendMessage("§e - Vars enregistrées (WeakRef): " + Var.vars.size());
                s.sendMessage("§e - Chargements Asynchrones (asyncLoads): " + Var.asyncLoads.size());
                s.sendMessage("§e - Vars persistantes en mémoire: " + Var.shouldStayLoadedVars.size());

                if (isAdvanced) {
                    s.sendMessage("§6 --- DÉTAILS DES VARS RETENUES (TRIÉES PAR CLÉ) ---");

                    java.util.List<Map.Entry<String, Var>> activeVars;
                    synchronized (Var.vars) {
                        activeVars = Var.vars.entrySet().stream()
                                .map(e -> Map.entry(e.getKey(), e.getValue().get()))
                                .filter(e -> e.getValue() != null)
                                .sorted(Map.Entry.comparingByKey())
                                .toList();
                    }

                    s.sendMessage("§7 - Instances vivantes réelles: §f" + activeVars.size());

                    if (activeVars.isEmpty()) {
                        s.sendMessage("§7 Aucune instance Var vivante en mémoire.");
                    } else {
                        for (Map.Entry<String, Var> entry : activeVars) {
                            String key = entry.getKey();
                            Var varObj = entry.getValue();
                            int keysCount = varObj.getKeys().size();
                            boolean isPersistent = Var.shouldStayLoadedVars.contains(varObj);

                            // --- ÉVALUATION DU SUPPLIER ---
                            CompletableFuture<Boolean> future = varObj.shouldStayLoaded();
                            String supplierStatus;
                            if (future == null) {
                                supplierStatus = "§7Aucun";
                            } else if (future.isDone()) {
                                try {
                                    Boolean result = future.getNow(null);
                                    supplierStatus = Boolean.TRUE.equals(result) ? "§aTrue" : "§cFalse";
                                } catch (Exception ex) {
                                    supplierStatus = "§cErreur";
                                }
                            } else {
                                supplierStatus = "§eEn attente";
                            }

                            s.sendMessage("§7 - §f" + key
                                    + " §7| Données: §e" + keysCount
                                    + " §7| Persistant: §e" + (isPersistent ? "Oui" : "Non")
                                    + " §7| Supplier: " + supplierStatus);
                        }
                    }

                    if (!Var.asyncLoads.isEmpty()) {
                        s.sendMessage("§c --- CHARGEMENTS ASYNCHRONES (TRIÉS) ---");
                        synchronized (Var.asyncLoads) {
                            Var.asyncLoads.keySet().stream()
                                    .sorted()
                                    .forEach(k -> s.sendMessage("§7   • §f" + k));
                        }
                    }
                }
            }
            case "gui" -> {
                s.sendMessage("§e=== Cache GUI (" + (isAdvanced ? "Avancé" : "Normal") + ") ===");
                s.sendMessage("§e - Inventaires actifs (WeakRef): " + GuiManager.guis.size());
                s.sendMessage("§e - Références fortes (guiReferences): " + GuiManager.guiReferences.size());
                s.sendMessage("§e - Réutilisables (reuseGuis): " + GuiManager.reuseGuis.size());
                s.sendMessage("§e - Joueurs visionnant un GUI: " + GuiManager.getActiveViewersCount());

                if (isAdvanced) {
                    s.sendMessage("§6 --- DÉTAILS DES GUIS EN MÉMOIRE ---");
                    Map<String, Long> grouped = GuiManager.getGuisGroupedByTitle();
                    if (grouped.isEmpty()) {
                        s.sendMessage("§7 Aucune instance enregistrée.");
                    } else {
                        grouped.forEach((title, count) ->
                                s.sendMessage("§7 - §f" + title + " §7: §e" + count + " instance(s)")
                        );
                    }
                }
            }
            case "utils" -> {
                s.sendMessage("§e - OnlinePlayerUUIDs: " + Utils.onlinePlayerNameCache.size());
                s.sendMessage("§e - OnlinePlayerNames: " + Utils.onlinePlayerNameCache.size());
                s.sendMessage("§e - OfflinePlayerUUIDs: " + Utils.offlinePlayerUUIDCache.size());
                s.sendMessage("§e - OfflinePlayerNameCache: " + Utils.offlinePlayerNameCache.size());
            }
            default -> s.sendMessage("§cVeuillez indiquez un argument valide. (/core cachesize <listeners|var|gui|utils> [normal|advanced])");
        }
    }

    private static void version(@NotNull CommandSender s){
        s.sendMessage("Vérification de la version, veuillez patienter...");
        Core.getServerImplementation().async().runNow(()->{
            try{
                final String latestTag= Updater.getLatestTag();
                final String currentVersion=Core.getInstance().getPluginMeta().getVersion();
                if(Updater.isNewerVersion(Updater.getLatestTag(),currentVersion)){
                    Core.getServerImplementation().global().run(()->{
                        final String url="https://github.com/"+Updater.USER+"/"+Updater.REPO+"/releases/latest";
                        final Component message=Component.text("Téléchargement: ", NamedTextColor.YELLOW)
                                .append(Component.text(url,NamedTextColor.GOLD)
                                        .hoverEvent(HoverEvent.showText(Component.text("Clique pour ouvrir",NamedTextColor.YELLOW)))
                                        .clickEvent(ClickEvent.openUrl(url)));

                        s.sendMessage("§e Une nouvelle version de NexusCore est disponible !");
                        s.sendMessage("§e Version installée: §6v"+currentVersion);
                        s.sendMessage("§e Nouvelle version: §6"+latestTag);
                        s.sendMessage(message);
                    });
                }else{
                    s.sendMessage("§e Dernière version installée !");
                    s.sendMessage("§e Version installée: §6v"+currentVersion);
                }
            }catch(Exception ex){
                s.sendMessage("§cImpossible de vérifier les mises à jour: "+ex.getMessage());
            }
        });
    }

    private static void cleanup(@NotNull CommandSender c){
        final long time=System.currentTimeMillis();
        Bukkit.getPluginManager().callEvent(new CoreCleanupEvent());
        c.sendMessage("Temps de cleanup: "+(System.currentTimeMillis()-time)+" ms !");
    }

    private static void meshHandler(@NotNull CommandSender c,@NotNull String[]args){
        if(args.length<2||!args[1].equalsIgnoreCase("save")){
            c.sendMessage("§cVeuillez indiquez un argument valide. (/core mesh <save>)");
            return;
        }

        final long startMillis=System.currentTimeMillis();

        VarObjectBackend.cleanVarObjectMap();

        CompletableFuture.allOf(
                VarObjectBackend.varObjects.values().stream()
                        .map(WeakReference::get)
                        .filter(Objects::nonNull)
                        .map(varObject -> varObject.getVar().saveAsync())
                        .toArray(CompletableFuture[]::new)
        ).thenRun(()->
                c.sendMessage("Les meshs ont été sauvegardés en "+(System.currentTimeMillis()-startMillis)+" ms !")
        ).exceptionally(ex->{
            c.sendMessage("La sauvegarde des meshs à rencontrées une érreur: "+ex.getMessage());
            return null;
        });
    }
}