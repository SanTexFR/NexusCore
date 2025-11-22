package fr.nexus.api.var.types.parents.normal.bukkit.itemstack;

import fr.nexus.api.var.types.parents.normal.VarType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class ItemStackType extends VarType<ItemStack>{
    //VARIABLES (STATICS)
    private static final ItemStackAccessor ACCESSOR = detectAccessor();
    private static ItemStackAccessor detectAccessor() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.Scheduler");
            return new FoliaItemStackAccessor();
        } catch (ClassNotFoundException e) {
            return new PaperItemStackAccessor();
        }
    }

    //CONSTRUCTOR
    public ItemStackType(){
        super(ItemStack.class,1);
    }


    //METHODS
    public byte@NotNull[] serializeSync(@Nullable ItemStack value) {
        return addVersionToBytes(ACCESSOR.serializeSync(value));
    }

    public@NotNull ItemStack deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull ItemStack deserialize(int version, byte[]bytes){
        if(version==1){
            return ACCESSOR.deserializeSync(bytes);
        }else throw createUnsupportedVersionException(version);
    }
}