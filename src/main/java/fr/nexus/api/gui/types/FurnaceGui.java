package fr.nexus.api.gui.types;

import fr.nexus.api.gui.Gui;
import fr.nexus.api.gui.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FurnaceGui extends Gui {

    // Un four a 3 emplacements, on le considère logiquement comme une largeur de 3 et une hauteur de 1
    public FurnaceGui(@Nullable InventoryHolder owner, @Nullable Component title) {
        super(owner, InventoryType.FURNACE, title, 3, 1);
    }

    public FurnaceGui(@Nullable Component title) {
        this(null, title);
    }

    // Méthodes spécifiques pour le four
    public void setIngredient(@NotNull GuiItem item) {
        addGuiItem(0, item);
    }

    public void setFuel(@NotNull GuiItem item) {
        addGuiItem(1, item);
    }

    public void setResult(@NotNull GuiItem item) {
        addGuiItem(2, item);
    }
}