package fr.nexus.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class GuiItem{
    //VARIABLES
    private@NotNull ItemStack item;
    private@Nullable BiConsumer<@NotNull InventoryClickEvent,@NotNull Player>playerAction;

    //CONSTRUCTOR
    public GuiItem(@NotNull ItemStack item){
        this.item=item;
    }
    public GuiItem(@NotNull ItemStack item,@Nullable BiConsumer<@NotNull InventoryClickEvent,@NotNull Player>playerAction){
        this.item=item;
        this.playerAction=playerAction;
    }

    //METHODS (INSTANCES)

    //ITEM-STACK
    public@NotNull ItemStack getItem(){
        return this.item;
    }
    public void setItem(@NotNull ItemStack item){
        this.item=item;
    }

    //ACTION
    public@Nullable BiConsumer<@NotNull InventoryClickEvent,@NotNull Player>getPlayerAction(){
        return this.playerAction;
    }
    public void setPlayerAction(@Nullable BiConsumer<@NotNull InventoryClickEvent,@NotNull Player>action){
        this.playerAction=action;
    }
}