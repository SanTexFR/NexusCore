package fr.nexus.listeners.events;

import fr.nexus.listeners.Listeners;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class CoreBlockBreakEvent extends CoreEvent<BlockBreakEvent>{
    //VARIABLES (STATICS)
    static{
        Listeners.registerCoreBridge(BlockBreakEvent.class,CoreBlockBreakEvent::new);
    }

    //VARIABLES (INSTANCES)
    private final@NotNull ItemStack item=getBukkitEvent().getPlayer().getInventory().getItemInMainHand();
    private final@NotNull BlockData blockData=getBukkitEvent().getBlock().getBlockData();

    //CONSTRUCTOR
    public CoreBlockBreakEvent(@NotNull BlockBreakEvent e) {
        super(e);
    }


    //METHODS (OVERRIDES)
    @Override
    public boolean applyResult(){
        final BlockBreakEvent e=getBukkitEvent();
        final Block block=e.getBlock();

        if(isAsyncCancelled()||!block.getBlockData().equals(this.blockData))return false;

        final GameMode gameMode=e.getPlayer().getGameMode();

        if(!gameMode.equals(GameMode.SURVIVAL)&&!gameMode.equals(GameMode.ADVENTURE))
            block.setType(Material.AIR);
        else{
            block.breakNaturally(this.item);
            if(this.item.getType().getMaxDurability()>0)
                this.item.damage(1,e.getPlayer());
        }return true;
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