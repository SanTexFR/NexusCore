package fr.nexus.api.gui.panels;

import fr.nexus.system.internal.performanceTracker.PerformanceTracker;
import fr.nexus.api.gui.GuiItem;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class GuiPage extends GuiPanel{
    //CONSTRUCTOR
    public GuiPage(int x1,int y1,int x2,int y2){
        super(x1,y1,x2,y2);
    }
    public GuiPage(int slot1,int slot2){
        super(slot1,slot2);
    }

    //METHODS (INSTANCES)
    public@NotNull List<@NotNull GuiItem> getGuiItemsInPanel(){
        final int slots=getSlotAmount();
        final int startIndex=slots*(getIndex()-1);

        final int endIndex=Math.min(startIndex+slots,getGuiItemAmount());
        return Collections.unmodifiableList(getGuiItems().subList(startIndex,endIndex));
    }
    public@Nullable GuiItem getGuiItemAt(int slot){
        final Integer relativeSlot=getRelativeSlot(slot);
        if(relativeSlot==null)return null;

        final int resolved=resolveIndex(relativeSlot);
        return resolved==-1?getBackground():getGuiItems().get(resolved);
    }
    private int resolveIndex(int relativeSlot){
        if(getGuiItems().isEmpty())return-1;

        final int globalIndex=(getIndex()-1)*getSlotAmount()+relativeSlot;
        return globalIndex>=getGuiItemAmount()?-1:globalIndex;
    }

    public void addGuiItemAtSlot(int slot,@NotNull GuiItem item){
        final Integer relativeSlot=getRelativeSlot(slot);
        if(relativeSlot==null){
            getGuiItems().add(item);
            return;
        }

        final int realIndex=resolveIndexForModification(relativeSlot);
        getGuiItems().add(realIndex,item);
    }

    public void removeGuiItemAtSlot(int slot){
        if(getGuiItems().isEmpty())return;

        final Integer relativeSlot=getRelativeSlot(slot);
        if(relativeSlot==null)return;

        final int realIndex=resolveIndexForModification(relativeSlot);
        getGuiItems().remove(realIndex);
    }

    private int resolveIndexForModification(int relativeSlot){
        if(getGuiItems().isEmpty())return 0;

        final int globalIndex=(getIndex()-1)*getSlotAmount()+relativeSlot;
        return Math.min(globalIndex,getGuiItemAmount());
    }

    //UTILS
    public@Nullable Integer getGlobalIndex(int slot){
        final Integer relativeSlot=getRelativeSlot(slot);
        if(relativeSlot==null)return null;

        final int globalIndex=relativeSlot+getSlotAmount()*(getIndex()-1);

        return globalIndex>=getGuiItemAmount()?null:globalIndex;
    }

    //INCREASES
    public boolean hasNext(int amount){
        final int totalPages=(getGuiItemAmount()+getSlotAmount()-1)/getSlotAmount();
        return getIndex()+Math.max(1,amount)<=totalPages;
    }
    public boolean hasPrevious(int amount){
        return getIndex()>Math.max(1,amount);
    }

    public void next(){
        next(1);
    }
    public void next(int amount){
        if(hasNext(amount))setIndex(getIndex()+amount);
        else if(isRecursive())setIndex(1);
    }
    public void previous(){
        previous(1);
    }
    public void previous(int amount){
        if(hasPrevious(amount))setIndex(getIndex()-amount);
        else if(isRecursive())setIndex((getGuiItemAmount()+getSlotAmount()-1)/getSlotAmount());
    }

    public void setIndex(int index){
        final int maxIndex=(getGuiItemAmount()+getSlotAmount()-1)/getSlotAmount();
        super.setIndex(Math.max(1,Math.min(maxIndex,index)));
    }

    //UPDATE
    public void update(){
        final long nanoTime=System.nanoTime();

        final Inventory inv=getInventory();
        final GuiItem background=getBackground();

        for(final Integer slot:getSlotsInPanel()){
            final GuiItem guiItem=getGuiItemAt(slot);
            if(guiItem==null){
                if(background==null){
                    PerformanceTracker.increment(PerformanceTracker.Types.GUI,"update",System.nanoTime()-nanoTime);
                    return;
                }

                if(!background.getItem().getType().equals(Material.AIR))inv.setItem(slot,background.getItem());

                continue;
            }

            if(!guiItem.getItem().getType().equals(Material.AIR))inv.setItem(slot,guiItem.getItem());
        }

        PerformanceTracker.increment(PerformanceTracker.Types.GUI,"update",System.nanoTime()-nanoTime);
    }
}