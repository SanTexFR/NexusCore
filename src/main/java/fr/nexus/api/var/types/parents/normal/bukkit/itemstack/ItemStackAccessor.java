package fr.nexus.api.var.types.parents.normal.bukkit.itemstack;

import org.bukkit.inventory.ItemStack;

@SuppressWarnings({"unused","UnusedReturnValue"})
public interface ItemStackAccessor{
    byte[] serializeSync(ItemStack item);
    ItemStack deserializeSync(byte[] bytes);
}