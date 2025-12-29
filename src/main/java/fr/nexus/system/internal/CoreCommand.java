package fr.nexus.system.internal;

import fr.nexus.Core;
import fr.nexus.api.command.CommandCreator;
import fr.nexus.api.command.tabcompleter.TabCompleterHandler;
import fr.nexus.api.gui.GuiManager;
import fr.nexus.api.listeners.Listeners;
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
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
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
                                c.sendMessage("§cVeuillez indiquez un argument valide. (/core <config, cachesize, version, mesh>)");
                                return;
                            }

                            switch(args[0].toLowerCase()){
                                case"config"->{
                                    if(args.length<2||!args[1].equalsIgnoreCase("reload")){
                                        c.sendMessage("§cVeuillez indiquez un argument valide. (/core config <reload> <safe, nosafe>)");
                                        return;
                                    }

                                    reloadConfiguration(c,args);
                                }case"cachesize"->{
                                    if(args.length<2||!args[1].equalsIgnoreCase("listeners")&&!args[1].equalsIgnoreCase("var")&&!args[1].equalsIgnoreCase("gui")&&!args[1].equalsIgnoreCase("utils")){
                                        c.sendMessage("§cVeuillez indiquez un argument valide. (/core cachesize <listeners, var, gui, utils>)");
                                        return;
                                    }
                                    
                                    cacheSize(c,args[1]);
                                }case"mesh"->{
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
                                            c.sendMessage("✅ Mesh saves "+(System.currentTimeMillis()-startMillis)+" ms !")
                                    ).exceptionally(ex->{
                                        c.sendMessage("❌ Mesh saves error: "+ex.getMessage());
                                        return null;
                                    });
                                }case"version"->version(c);
                                default->c.sendMessage("§cCommande incorrecte. (/core <config, cachesize, version, mesh>)");
                            }
                        })
                        .ifPlayer(p->{
                            if(args.length<1){
                                p.sendMessage("§cVeuillez indiquez un argument valide. (/core <config, performance, cachesize, information, version, mesh>)");
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
                                        p.sendMessage("§cVeuillez indiquez un argument valide. (/core cachesize <listeners, var, gui, utils>)");
                                        return;
                                    }

                                    cacheSize(p,args[1]);
                                }case"mesh"->{
                                    if(args.length<2||!args[1].equalsIgnoreCase("save")){
                                        p.sendMessage("§cVeuillez indiquez un argument valide. (/core mesh <save>)");
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
                                            p.sendMessage("✅ Mesh saves "+(System.currentTimeMillis()-startMillis)+" ms !")
                                    ).exceptionally(ex->{
                                        p.sendMessage("❌ Mesh saves error: "+ex.getMessage());
                                        return null;
                                    });
                                }case"information"->
                                    InformationGui.primaryGui(p);
                                case"version"->version(p);
                                default->p.sendMessage("§cCommande incorrecte. (/core <performance, config, cachesize, version, mesh>)");
                            }
                        })
                ).perform();

        TabCompleterHandler.create("core").addDisplay(sender->
            ()->Set.of("performance","config","cachesize","version","information","mesh")).perform();

        TabCompleterHandler.create("core").addArg(sender->()->"config").addDisplay(sender->
                ()->Set.of("reload")).perform();
        TabCompleterHandler.create("core").addArg(sender->()->"config").addArg(sender->()->"reload").addDisplay(sender->
                ()->Set.of("safe","nosafe")).perform();
        TabCompleterHandler.create("core").addArg(sender->()->"mesh").addDisplay(sender->
                ()->Set.of("save")).perform();
        TabCompleterHandler.create("core").addArg(sender->()->"performance").addDisplay(sender->
                ()->Set.of("gui")).perform();
        TabCompleterHandler.create("core").addArg(sender->()->"cachesize").addDisplay(sender->
                ()->Set.of("listeners","var","gui","utils")).perform();
    }
    private static void reloadConfiguration(@NotNull CommandSender sender,@NotNull String[]args){
        boolean nosafe=(args.length>2&&args[2].equalsIgnoreCase("nosafe"));

        final long time=System.currentTimeMillis();
        Core.reload(nosafe);

        if(nosafe)sender.sendMessage("§eConfiguration non sécurisée rechargée en "+(System.currentTimeMillis()-time)+"ms !");
        else sender.sendMessage("§eConfiguration sécurisée rechargée en "+(System.currentTimeMillis()-time)+"ms !");
    }
    private static void cacheSize(@NotNull CommandSender s,@NotNull String arg){
        switch(arg.toLowerCase()){
            case"listeners"->{
                //SYNC
                s.sendMessage("§e - SyncTypeAmount: "+Listeners.syncEventsRegistered.size());

                final int[]syncAmount={0};
                Listeners.syncEventsRegistered.forEach((key,value)->syncAmount[0]+=value.size());
                s.sendMessage("§e - SyncAmount: "+syncAmount[0]);

                //ASYNC
                s.sendMessage("§e - AsyncTypeAmount: "+Listeners.asyncEventsRegistered.size());

                final int[]asyncAmount={0};
                Listeners.asyncEventsRegistered.forEach((key,value)->asyncAmount[0]+=value.size());
                s.sendMessage("§e - SyncAmount: "+asyncAmount[0]);
            }
            case"var"->{
                s.sendMessage("§e - Vars: "+Var.vars.size());
                s.sendMessage("§e - AsyncLoads: "+Var.vars.size());
            }case"gui"->{
                s.sendMessage("§e - Guis: "+GuiManager.guis.size());
                s.sendMessage("§e - GuiReferences: "+GuiManager.guiReferences.size());
            }case"utils"->{
                s.sendMessage("§e - OnlinePlayerUUIDs: "+Utils.onlinePlayerNameCache.size());
                s.sendMessage("§e - OnlinePlayerNames: "+Utils.onlinePlayerNameCache.size());
                s.sendMessage("§e - OfflinePlayerUUIDs: "+Utils.offlinePlayerUUIDCache.size());
                s.sendMessage("§e - OfflinePlayerNames: "+Utils.offlinePlayerNameCache.size());
            }default->s.sendMessage("§cVeuillez indiquez un argument valide. (/core cachesize <var,gui,utils>)");
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
}