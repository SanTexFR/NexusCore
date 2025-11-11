package fr.nexus.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class GuiItem{
    //VARIABLES
    private@NotNull ItemStack item;
    private@Nullable Consumer<@NotNull InventoryClickEvent>action;

    //CONSTRUCTOR
    public GuiItem(@NotNull ItemStack item){
        this.item=item;
    }
    public GuiItem(@NotNull ItemStack item,@Nullable Consumer<@NotNull InventoryClickEvent>action){
        this.item=item;
        this.action=action;
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
    public@Nullable Consumer<@NotNull InventoryClickEvent>getAction(){
        return this.action;
    }
    public void setAction(@Nullable Consumer<@NotNull InventoryClickEvent>action){
        this.action=action;
    }
}