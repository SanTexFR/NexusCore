package fr.nexus.api.gui.types;

import fr.nexus.api.gui.Gui;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Nullable;

public class DispenserGui extends Gui {

    // Une grille parfaite de 3x3 (comme une table de craft)
    public DispenserGui(@Nullable InventoryHolder owner, @Nullable Component title) {
        super(owner, InventoryType.DISPENSER, title, 3, 3);
    }

    public DispenserGui(@Nullable Component title) {
        this(null, title);
    }
}