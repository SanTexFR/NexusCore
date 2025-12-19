package fr.nexus.system.internal.information;

import com.cjcrafter.foliascheduler.GlobalSchedulerImplementation;
import fr.nexus.Core;
import fr.nexus.api.gui.Gui;
import fr.nexus.api.gui.panels.GuiPage;
import fr.nexus.api.itembuilder.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class InformationGui{
    //PRIMARY
    public static void primaryGui(@NotNull Player p){
        //GUI
        final Gui gui=new Gui(5,Component.text("Menu d'informations"));

        gui.setInventoryClickEvent(e->e.setCancelled(true));
        gui.setBackground(Material.BLACK_STAINED_GLASS_PANE);

        gui.setEffectiveCooldownMs(500L);

        //GUI-PAGE
        final GuiPage guiPage=new GuiPage(1,1,7,3);
        gui.addGuiPage("pagination",guiPage);
        guiPage.setBackground(Material.WHITE_STAINED_GLASS_PANE);

        //PAGE-ITEM
        final GlobalSchedulerImplementation scheduler=Core.getServerImplementation().global();
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);

        for (Information info : InformationHandler.informations) {
            chain = chain.thenCompose(v ->
                    info.supplier().get()
                            .thenAccept(lores ->
                                    scheduler.run(() ->
                                            guiPage.addGuiItem(
                                                    ItemBuilder.createItem(info.material())
                                                            .setDisplayName(info.title())
                                                            .setLore(lores)
                                                            .build()
                                            )
                                    )
                            )
                            .exceptionally(ex -> {
                                ex.printStackTrace();
                                return null;
                            })
            );
        }

        //OTHERS
        chain.thenRun(() ->
                scheduler.runDelayed(() -> {
                    gui.generalUpdate();
                    if (p.isOnline()) gui.display(p);
                }, 4L)
        ).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }
}