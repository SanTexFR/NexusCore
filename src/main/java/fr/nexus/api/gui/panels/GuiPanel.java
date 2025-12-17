package fr.nexus.api.gui.panels;

import fr.nexus.api.gui.GuiItem;
import fr.nexus.api.gui.modules.GuiBackground;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings({"unused","UnusedReturnValue"})
public abstract sealed class GuiPanel implements GuiBackground permits GuiPage,GuiSlider{
    //VARIABLES (STATICS)
    private Inventory inventory;
    private final@NotNull List<@NotNull GuiItem>guiItems=new ArrayList<>();

    private@Nullable GuiItem background;
    private boolean recursive;

    //VARIABLES (INSTANCES)
    private int x1,y1,x2,y2;
    private int slot1,slot2;

    private@NotNull List<@NotNull Integer>slotsInPanel=new ArrayList<>();

    private int index=1;

    //CONSTRUCTOR
    protected GuiPanel(int x1,int y1,int x2,int y2){
        defineSlots(x1,y1,x2,y2);
    }
    protected GuiPanel(int slot1,int slot2){
        defineSlots(slot1,slot2);
    }


    //METHODS

    //RECURSION
    public void setRecursive(boolean state){
        this.recursive=state;
    }
    public boolean isRecursive(){
        return this.recursive;
    }

    //INVENTORY
    public@NotNull Inventory getInventory(){
        return this.inventory;
    }
    public void setInventory(@Nullable Inventory inventory){
        this.inventory=inventory;
    }

    //SLOTS
    public int getPanelFirstSlot(){
        return this.slot1;
    }
    public int getPanelSecondSlot(){
        return this.slot2;
    }

    public void defineSlots(int x1,int y1,int x2,int y2){
        if(x2<x1||y2<y1)throw new IllegalArgumentException("Invalid rectangle coordinates");

        this.x1=x1;
        this.y1=y1;

        this.x2=x2;
        this.y2=y2;

        this.slot1=y1*9+x1;
        this.slot2=y2*9+x2;

        defineSlotsInPanel();
    }
    public void defineSlots(int slot1,int slot2){
        if(slot2<slot1)throw new RuntimeException("slot2 cannot be lower than slot");

        this.slot1=slot1;
        this.slot2=slot2;

        this.x1=slot1%9;
        this.y1=slot1/9;

        this.x2=slot2%9;
        this.y2=slot2/9;

        defineSlotsInPanel();
    }

    //ITEMS
    public@NotNull List<@NotNull GuiItem>getGuiItems(){
        return this.guiItems;
    }

    public abstract @NotNull List<@NotNull GuiItem>getGuiItemsInPanel();


    public int getGuiItemAmount(){
        return this.guiItems.size();
    }
    public void clearGuiItems(){
        this.guiItems.clear();
    }
    public void clear(){
        clearGuiItems();
        this.index=1;
    }

    //DIRECTLY ITEMS
    public void addGuiItem(@NotNull GuiItem item){
        this.guiItems.add(item);
    }
    public void addGuiItem(@NotNull ItemStack item){
        this.guiItems.add(new GuiItem(item));
    }
    public void addGuiItem(@NotNull ItemStack item,@Nullable Consumer<@NotNull InventoryClickEvent>action){
        this.guiItems.add(new GuiItem(item,action));
    }

    public void addGuiItemAtSlot(int x,int y,@NotNull GuiItem item){
        addGuiItemAtSlot(y*9+x,item);
    }
    public void addGuiItemAtSlot(int x,int y,@NotNull ItemStack item){
        addGuiItemAtSlot(y*9+x,new GuiItem(item));
    }
    public void addGuiItemAtSlot(int x,int y,@NotNull ItemStack item,@Nullable Consumer<@NotNull InventoryClickEvent>action){
        addGuiItemAtSlot(y*9+x,new GuiItem(item,action));
    }

    public abstract void addGuiItemAtSlot(int slot,@NotNull GuiItem item);

    public void addGuiItemAtIndex(int index,@NotNull GuiItem item){
        this.guiItems.add(index,item);
    }
    public void addGuiItemAtIndex(int index,@NotNull ItemStack item){
        this.guiItems.add(index,new GuiItem(item));
    }
    public void addGuiItemAtIndex(int index,@NotNull ItemStack item,@Nullable Consumer<@NotNull InventoryClickEvent>action){
        this.guiItems.add(index,new GuiItem(item,action));
    }

    public void removeGuiItemAtSlot(int x,int y){
        removeGuiItemAtSlot(y*9+x);
    }
    public abstract void removeGuiItemAtSlot(int slot);
    public void removeGuiItemAtIndex(int index){
        this.guiItems.remove(index);
    }

    public@Nullable GuiItem getGuiItemAt(int x,int y){
        return getGuiItemAt(y*9+x);
    }
    public abstract GuiItem getGuiItemAt(int slot);

    //BACKGROUND
    public@Nullable GuiItem getBackground(){
        return this.background;
    }
    public void setBackground(@Nullable GuiItem guiItem){
        this.background=guiItem;
    }

    //INDEX
    public int getIndex(){
        return this.index;
    }
    public void setIndex(int index){
        this.index=index;
    }

    //UTILS
    public int getWidth(){return this.x2-this.x1+1;}
    public int getHeight(){return this.y2-this.y1+1;}

    public int getSlotAmount(){
        return getWidth()*getHeight();
    }

    private void defineSlotsInPanel(){
        final List<Integer>slots=new ArrayList<>();
        for(int y=this.y1;y<=this.y2;y++)
            for(int x=this.x1;x<=this.x2;x++)
                slots.add(y*9+x);
        this.slotsInPanel=slots;
    }
    public@NotNull List<@NotNull Integer>getSlotsInPanel(){
        return this.slotsInPanel;
    }

    public@Nullable Integer getRelativeSlot(int slot) {
        if(!isWithin(slot))return null;

        final int x=slot%9;
        final int y=slot/9;

        final int relativeX=x-this.x1;
        final int relativeY=y-this.y1;

        return relativeY*getWidth()+relativeX;
    }

    public boolean isWithin(int slot){
        return isWithin(slot%9,slot/9);
    }
    public boolean isWithin(int x,int y){
        return x>=this.x1&&x<=this.x2&&y>=this.y1&&y<=this.y2;
    }

    public@Nullable Integer getGlobalIndex(int x,int y){
        return getGlobalIndex(y*9+x);
    }
    public abstract@Nullable Integer getGlobalIndex(int slot);

    public boolean isSlotOccupied(int slot){
        final Integer relativeSlot=getRelativeSlot(slot);
        if(relativeSlot==null)return false;

        final List<GuiItem>itemsInPanel=getGuiItemsInPanel();
        return relativeSlot<itemsInPanel.size()&&itemsInPanel.get(relativeSlot)!=null;
    }

    //INCREASES
    public abstract boolean hasNext(int amount);
    public abstract boolean hasPrevious(int amount);

    public abstract void next();
    public abstract void next(int amount);
    public abstract void previous();
    public abstract void previous(int amount);

    public abstract int increments();
    public abstract int decrements();

    //MAIN (UPDATE)
    public abstract void update();
}