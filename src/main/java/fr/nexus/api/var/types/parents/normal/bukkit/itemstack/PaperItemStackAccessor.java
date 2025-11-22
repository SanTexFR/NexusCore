//package fr.nexus.api.var.types.parents.normal.bukkit.itemstack;
//
//import org.bukkit.Material;
//import org.bukkit.inventory.ItemStack;
//import org.jetbrains.annotations.NotNull;
//
//@SuppressWarnings({"unused","UnusedReturnValue"})
//public class PaperItemStackAccessor implements ItemStackAccessor {
//    private static final @NotNull ItemStack AIR_ITEM_STACK=new ItemStack(Material.AIR);
//
//    @Override
//    public byte[] serializeSync(ItemStack item) {
//        if(item == null || item.getType().isAir()) return new byte[]{(byte)0xff};
//        return item.serializeAsBytes();
//    }
//
//    @Override
//    public ItemStack deserializeSync(byte[] bytes) {
//        if(bytes.length == 0 || bytes[0] == (byte)0xff) return AIR_ITEM_STACK;
//        return ItemStack.deserializeBytes(bytes);
//    }
//}