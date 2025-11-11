package fr.nexus;

import fr.nexus.command.CommandCreator;
import fr.nexus.command.tabcompleter.TabCompleterHandler;
import fr.nexus.gui.GuiManager;
import fr.nexus.listeners.Listeners;
import fr.nexus.listeners.core.CoreInitializeEvent;
import fr.nexus.performanceTracker.PerformanceTrackerGui;
import fr.nexus.utils.Utils;
import fr.nexus.var.Var;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

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
                                c.sendMessage("§cVeuillez indiquez un argument valide. (/core <config,cachesize>)");
                                return;
                            }

                            switch(args[0].toLowerCase()){
                                case"config"->{
                                    if(args.length<2||!args[1].equalsIgnoreCase("reload")){
                                        c.sendMessage("§cVeuillez indiquez un argument valide. (/core config <reload>)");
                                        return;
                                    }

                                    reloadConfiguration(c);
                                }case"cachesize"->{
                                    if(args.length<2||!args[1].equalsIgnoreCase("listeners")&&!args[1].equalsIgnoreCase("var")&&!args[1].equalsIgnoreCase("gui")&&!args[1].equalsIgnoreCase("utils")){
                                        c.sendMessage("§cVeuillez indiquez un argument valide. (/core cachesize <listeners,var,gui,utils>)");
                                        return;
                                    }
                                    
                                    cacheSize(c,args[1]);
                                }default->c.sendMessage("§cCommande incorrecte. (/core <config,cachesize>)");
                            }
                        })
                        .ifPlayer(p->{
                            if(args.length<1){
                                p.sendMessage("§cVeuillez indiquez un argument valide. (/core <config,performance,cachesize>)");
                                return;
                            }

                            switch(args[0].toLowerCase()){
                                case"config"->{
                                    if(args.length<2||!args[1].equalsIgnoreCase("reload")){
                                        p.sendMessage("§cVeuillez indiquez un argument valide. (/core config <reload>)");
                                        return;
                                    }

                                    reloadConfiguration(p);
                                }case"performance"->{
                                    if(args.length<2||!args[1].equalsIgnoreCase("gui")){
                                        p.sendMessage("§cVeuillez indiquez un argument valide. (/core performance <gui>)");
                                        return;
                                    }

                                    PerformanceTrackerGui.primaryGui(p);
                                }case"cachesize"->{
                                    if(args.length<2||!args[1].equalsIgnoreCase("listeners")&&!args[1].equalsIgnoreCase("var")&&!args[1].equalsIgnoreCase("gui")&&!args[1].equalsIgnoreCase("utils")){
                                        p.sendMessage("§cVeuillez indiquez un argument valide. (/core cachesize <listeners,var,gui,utils>)");
                                        return;
                                    }

                                    cacheSize(p,args[1]);
                                }default->p.sendMessage("§cCommande incorrecte. (/core <config,performance>)");
                            }
                        })
                ).perform();

        TabCompleterHandler.create("core").addDisplay(sender->
            ()->Set.of("performance","config","cachesize")).perform();

        TabCompleterHandler.create("core").addArg(sender->()->"config").addDisplay(sender->
                ()->Set.of("reload")).perform();
        TabCompleterHandler.create("core").addArg(sender->()->"performance").addDisplay(sender->
                ()->Set.of("gui")).perform();
        TabCompleterHandler.create("core").addArg(sender->()->"cachesize").addDisplay(sender->
                ()->Set.of("listeners","var","gui","utils")).perform();
    }
    private static void reloadConfiguration(@NotNull CommandSender sender){
        final long time=System.currentTimeMillis();
        Core.reload(false);
        sender.sendMessage("§eConfiguration rechargée en "+(System.currentTimeMillis()-time)+"ms !");
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
}