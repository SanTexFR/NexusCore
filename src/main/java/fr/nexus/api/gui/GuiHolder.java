package fr.nexus.api.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class GuiHolder implements InventoryHolder {
    private final Gui gui;
    public GuiHolder(Gui gui) { this.gui = gui; }
    @Override public @NotNull Inventory getInventory() { return gui.getInventory(); }
}