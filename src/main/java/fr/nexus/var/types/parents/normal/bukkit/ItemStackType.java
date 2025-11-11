package fr.nexus.var.types.parents.normal.bukkit;

import fr.nexus.var.types.parents.normal.VarType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class ItemStackType extends VarType<ItemStack>{
    //VARIABLES (STATICS)
    private static final @NotNull ItemStack AIR_ITEM_STACK=new ItemStack(Material.AIR);

    //CONSTRUCTOR
    public ItemStackType(){
        super(ItemStack.class,1);
    }


    //METHODS
    public byte@NotNull[] serializeSync(@Nullable ItemStack value) {
        if(value==null||value.getType()==Material.AIR)return new byte[]{(byte) 0xff};
        return addVersionToBytes(value.serializeAsBytes());
    }
    public@NotNull ItemStack deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull ItemStack deserialize(int version, byte[]bytes){
        if(version==1){
            if(bytes[0]==(byte)0xff)return AIR_ITEM_STACK;
            else return ItemStack.deserializeBytes(bytes);
        }else throw createUnsupportedVersionException(version);
    }
}