package fr.nexus.api.gui.modules;

import fr.nexus.api.gui.GuiItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

@SuppressWarnings({"unused","UnusedReturnValue"})
public interface GuiBackground{
    @Nullable GuiItem getBackground();
    void setBackground(@Nullable GuiItem guiItem);

    default void setBackground(@Nullable ItemStack item){
        setBackground(item!=null?new GuiItem(item):null);
    }
    default void setBackground(@Nullable ItemStack item,@Nullable BiConsumer<@NotNull InventoryClickEvent,@NotNull Player> playerAction){
        setBackground(item!=null?new GuiItem(item,playerAction):null);
    }

    default void setBackground(@Nullable Material material){
        setBackground(material!=null?new GuiItem(new ItemStack(material)):null);
    }
    default void setBackground(@Nullable Material material,@Nullable BiConsumer<@NotNull InventoryClickEvent,@NotNull Player> playerAction){
        setBackground(material!=null?new GuiItem(new ItemStack(material),playerAction):null);
    }
}