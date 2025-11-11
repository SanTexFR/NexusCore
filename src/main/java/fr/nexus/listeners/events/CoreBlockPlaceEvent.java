package fr.nexus.listeners.events;

import fr.nexus.listeners.Listeners;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class CoreBlockPlaceEvent extends CoreEvent<BlockPlaceEvent>{
    //VARIABLES (STATICS)
    static{
        Listeners.registerCoreBridge(BlockPlaceEvent.class,CoreBlockPlaceEvent::new);
    }

    //VARIABLES (INSTANCES)
    private final@NotNull ItemStack item;
    private final@NotNull BlockData blockData=getBukkitEvent().getBlock().getBlockData();

    //CONSTRUCTOR
    public CoreBlockPlaceEvent(@NotNull BlockPlaceEvent e) {
        super(e);

        this.item=new ItemStack(e.getPlayer().getInventory().getItem(e.getHand()));
        this.item.setAmount(1);
    }


    //METHODS (OVERRIDES)
    @Override
    public boolean applyResult(){
        if(isAsyncCancelled())return false;

        final BlockPlaceEvent e=getBukkitEvent();
        final Block block=e.getBlock();
        final Material blockType=block.getType();
        final Player p=e.getPlayer();
        final GameMode gameMode=p.getGameMode();
        final Material toolType=this.item.getType();

        if(!gameMode.equals(GameMode.SURVIVAL)&&!gameMode.equals(GameMode.ADVENTURE))
            return handleBlockAction(block,toolType);

        final PlayerInventory inv=p.getInventory();
        final ItemStack handItem=e.getItemInHand();

        //TESTER CA EN METTANT UN AUTRE ITEM EN MAIN DURANT
        if(handItem.isSimilar(this.item)){
            if(!handleBlockAction(block,toolType))return false;

            if(toolType.isBlock()){
                if(handItem.getAmount()>1)
                    handItem.setAmount(handItem.getAmount()-1);
                else inv.setItem(e.getHand(),null);
            }else handItem.damage(1,p);

            return true;
        }

        if(toolType.isBlock())
            if(!inv.removeItem(this.item).isEmpty())return true;
        else this.item.damage(1,p);

        if(toolType.isBlock())block.setBlockData(this.blockData);
        else block.setType(Material.DIRT_PATH);
        return true;
    }
    private boolean handleBlockAction(@NotNull Block block,@NotNull Material toolType){
        final Material blockType=block.getType();

        if(toolType.isBlock()){
            if(!blockType.equals(Material.AIR)&&blockType.isSolid())return false;
            block.setBlockData(this.blockData);
            return true;
        }

        if(Tag.ITEMS_SHOVELS.isTagged(toolType)){
            if(blockType!=Material.GRASS_BLOCK&&blockType!=Material.DIRT)return false;
            block.setType(Material.DIRT_PATH);
            return true;
        }

        if(Tag.ITEMS_HOES.isTagged(toolType)){
            switch(blockType){
                case COARSE_DIRT,ROOTED_DIRT->block.setType(Material.DIRT);
                case GRASS_BLOCK,DIRT->block.setType(Material.FARMLAND);
                default->{return false;}
            }return true;
        }

        return false;
    }

    @Override
    public@NotNull Object getKey() {
        final Block block=getBukkitEvent().getBlock();
        return (((long) block.getWorld().hashCode()) << 32)
                | (((long) (block.getX() & 0xFFF)) << 20)
                | (((long) (block.getY() & 0xFFF)) << 10)
                | ((long) (block.getZ() & 0x3FF));
    }
}