package fr.nexus.api.gui.types;

import fr.nexus.api.gui.Gui;
import fr.nexus.api.gui.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HopperGui extends Gui {

    // Un hopper a une grille de 5 emplacements sur 1 ligne
    public HopperGui(@Nullable InventoryHolder owner, @Nullable Component title) {
        super(owner, InventoryType.HOPPER, title, 5, 1);
    }

    public HopperGui(@Nullable Component title) {
        this(null, title);
    }

    // Possibilité de placer par 'x' très facilement
    public void setItem(int x, @NotNull GuiItem item) {
        addGuiItem(x, 0, item);
    }
}