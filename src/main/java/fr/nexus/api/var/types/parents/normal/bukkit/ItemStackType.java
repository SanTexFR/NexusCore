package fr.nexus.api.var.types.parents.normal.bukkit;

import fr.nexus.api.var.types.parents.InternalVarType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class ItemStackType extends InternalVarType<ItemStack>{

    private static final @NotNull ItemStack AIR_ITEM_STACK = new ItemStack(Material.AIR);

    @Override
    public byte @NotNull [] serializeSync(@Nullable ItemStack value) {
        if (value == null || value.getType() == Material.AIR) {
            return addVersionToBytes(new byte[0]);
        }

        // Sinon, Bukkit serialize
        return addVersionToBytes(value.serializeAsBytes());
    }

    @Override
    public @NotNull ItemStack deserializeSync(int version, byte[] bytes) {
        if (version == 1) {
            if (bytes.length == 0) return AIR_ITEM_STACK;

            try {
                return ItemStack.deserializeBytes(bytes);
            } catch (Exception e) {
                return AIR_ITEM_STACK;
            }
        } else {
            throw createUnsupportedVersionException(version);
        }
    }
}