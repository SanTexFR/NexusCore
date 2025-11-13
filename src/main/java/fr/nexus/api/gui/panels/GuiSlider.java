package fr.nexus.api.gui.panels;

import fr.nexus.system.internal.performanceTracker.PerformanceTracker;
import fr.nexus.api.gui.GuiItem;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class GuiSlider extends GuiPanel{
    //VARIABLES (INSTANCES)
    private boolean vertical;

    //CONSTRUCTOR
    public GuiSlider(int x1,int y1,int x2,int y2){
        super(x1,y1,x2,y2);
    }
    public GuiSlider(int slot1,int slot2){
        super(slot1,slot2);
    }


    //METHODS (INSTANCES)

    //VERTICAL
    public void setVertical(){
        this.vertical=true;
    }
    public boolean isVertical(){
        return this.vertical;
    }

    public void setHorizontal(){
        this.vertical=false;
    }
    public boolean isHorizontal(){
        return!this.vertical;
    }

    //GUI-ITEMS
    public@NotNull List<@NotNull GuiItem>getGuiItemsInPanel(){
        final int slots=getSlotAmount();
        final int size =getGuiItemAmount();
        final List<GuiItem>panelItems=new ArrayList<>(slots);

        if(size==0)return Collections.emptyList();

        for(int i=0;i<slots;i++){
            int idx;
            if(isHorizontal())
                idx=(getIndex()-1)+i;
            else{
                final int col=i%getWidth();
                final int row=i/getWidth();
                idx=(getIndex()-1)+col*getHeight()+row;
            }

            if(isRecursive())
                idx=((idx%size)+size)%size;
            else if(idx>=size)
                break;

            panelItems.add(getGuiItems().get(idx));
        }

        return Collections.unmodifiableList(panelItems);
    }
    public@Nullable GuiItem getGuiItemAt(int slot){
        final Integer relative=getRelativeSlot(slot);
        if(relative==null)return null;

        final int size=getGuiItemAmount();
        if(size==0)return getBackground();

        int idx=(getIndex()-1);
        if(isHorizontal())
            idx+=relative;
        else{
            final int col=relative%getWidth();
            final int row=relative/getWidth();
            idx+=col*getHeight()+row;
        }

        if(isRecursive())
            idx=((idx%size)+size)%size;
        else if(idx>=size)
            return getBackground();

        return getGuiItems().get(idx);
    }

    public void addGuiItemAtSlot(int slot,@NotNull GuiItem item){
        final Integer relative=getRelativeSlot(slot);
        if(relative==null){addGuiItem(item);return;}

        final int size=getGuiItemAmount();
        int insertAt=(getIndex()-1);
        if(isHorizontal())insertAt+=relative;
        else insertAt+=relative*getWidth();

        if(isRecursive()&&size>0)insertAt%=(size+1);
        insertAt=Math.min(insertAt,size);
        addGuiItemAtIndex(insertAt,item);
    }

    public void removeGuiItemAtSlot(int slot){
        final Integer relative=getRelativeSlot(slot);
        if(relative==null)return;

        final int size=getGuiItemAmount();
        if(size==0)return;

        int removeAt=(getIndex()-1);
        if(isHorizontal())removeAt+=relative;
        else removeAt+=relative*getWidth();

        if(isRecursive())removeAt%=size;
        else if(removeAt>=size)return;

        removeGuiItemAtIndex(removeAt);
    }

    //UTILS
    public@Nullable Integer getGlobalIndex(int slot){
        final Integer relative=getRelativeSlot(slot);
        if(relative==null)return null;

        final int size=getGuiItemAmount();
        if(size==0)return null;

        int globalIndex=(getIndex()-1);
        if(isHorizontal())
            globalIndex+=relative;
        else{
            final int col=relative%getWidth();
            final int row=relative/getWidth();
            globalIndex+=col*getHeight()+row;
        }

        if(isRecursive())
            globalIndex=((globalIndex%size)+size)%size;
        else if(globalIndex>=size)return null;

        return globalIndex;
    }


    //INCREASES
    public boolean hasNext(int amount) {
        if(isRecursive())return true;
        return(getIndex()-1)+getSlotAmount()+amount<=getGuiItemAmount();
    }
    public boolean hasPrevious(int amount){
        if(isRecursive())return true;
        return(getIndex()-1)-amount>=0;
    }

    public void next(){
        next(1);
    }
    public void next(int amount){
        if(isRecursive()){
            setIndex(getIndex()+amount);
            normalizeIndex();
        }else{
            final int maxStartIndex=Math.max(1,getGuiItemAmount()-getSlotAmount()+1);
            setIndex(Math.min(getIndex()+amount,maxStartIndex));
        }
    }
    public void previous(){
        previous(1);
    }
    public void previous(int amount){
        if(isRecursive()){
            setIndex(getIndex()-amount);
            normalizeIndex();
        }else setIndex(Math.max(1,getIndex()-amount));
    }
    private void normalizeIndex(){
        final int size=getGuiItemAmount();
        final int slots=getSlotAmount();
        if(!isRecursive()||size==0)return;

        if(getIndex()>size)setIndex(1);
        else if(getIndex()<1)setIndex(size);
    }


    //UPDATE
    public void update(){
        final long nanoTime=System.nanoTime();

        final Inventory inv=getInventory();
        final GuiItem bg=getBackground();

        for(final Integer slot:getSlotsInPanel()){
            final GuiItem gi=getGuiItemAt(slot);
            if(gi==null){
                if(bg!=null &&!bg.getItem().getType().equals(Material.AIR))
                    inv.setItem(slot,bg.getItem());
                continue;
            }
            if(!gi.getItem().getType().equals(Material.AIR))
                inv.setItem(slot, gi.getItem());
        }

        PerformanceTracker.increment(PerformanceTracker.Types.GUI,"update",System.nanoTime()-nanoTime);
    }
}