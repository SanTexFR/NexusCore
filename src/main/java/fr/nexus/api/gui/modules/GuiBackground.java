package fr.nexus.api.gui.modules;

import fr.nexus.api.gui.GuiItem;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings({"unused","UnusedReturnValue"})
public interface GuiBackground{
    @Nullable GuiItem getBackground();
    void setBackground(@Nullable GuiItem guiItem);

    default void setBackground(@Nullable ItemStack item){
        setBackground(item!=null?new GuiItem(item):null);
    }
    default void setBackground(@Nullable ItemStack item,@Nullable Consumer<@NotNull InventoryClickEvent> action){
        setBackground(item!=null?new GuiItem(item,action):null);
    }

    default void setBackground(@Nullable Material material){
        setBackground(material!=null?new GuiItem(new ItemStack(material)):null);
    }
    default void setBackground(@Nullable Material material,@Nullable Consumer<@NotNull InventoryClickEvent> action){
        setBackground(material!=null?new GuiItem(new ItemStack(material),action):null);
    }
}