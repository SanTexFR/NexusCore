package fr.nexus.api.itembuilder;

import fr.nexus.Core;
import fr.nexus.system.internal.performanceTracker.PerformanceTracker;
import fr.nexus.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings({"unused","UnusedReturnValue","deprecation"})
public class ItemBuilder {
    private static final @NotNull Random random = new Random();
    private static final List<Material> MATERIALS = Arrays.stream(Material.values()).filter(Material::isItem).toList();

    private final @NotNull ItemStack itemStack;
    private @NotNull ItemMeta meta;

    private ItemBuilder(@NotNull Material material) {
        this.itemStack = ItemStack.of(material);
        // On récupère la meta une seule fois au début
        ItemMeta m = this.itemStack.getItemMeta();
        if (m == null) throw new IllegalArgumentException("Material " + material + " has no ItemMeta");
        this.meta = m;
    }

    public static @NotNull ItemBuilder createItem(@NotNull Material material) {
        return new ItemBuilder(material);
    }

    public static @NotNull ItemBuilder createItem() {
        return new ItemBuilder(Material.DIRT);
    }

    public static @NotNull ItemBuilder cloneFrom(@NotNull ItemStack other) {
        final long nanoTime = System.nanoTime();
        final ItemBuilder builder = createItem(other.getType());

        ItemMeta otherMeta = other.getItemMeta();
        if (otherMeta != null) {
            builder.meta = otherMeta.clone();
        }
        builder.setQuantity(other.getAmount());

        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER, "cloneFrom", System.nanoTime() - nanoTime);
        return builder;
    }

    // --- NBT METHODS (STATICS) ---
    public static <T, Z> Z getNbt(@NotNull ItemStack item, @NotNull PersistentDataType<T, Z> dataType, @NotNull String key) {
        ItemMeta m = item.getItemMeta();
        if (m == null) return null;
        return m.getPersistentDataContainer().get(new NamespacedKey(Core.getInstance(), key), dataType);
    }

    public static <T, Z> @NotNull ItemStack addNbt(@NotNull ItemStack item, @NotNull PersistentDataType<T, Z> dataType, @NotNull String key, @NotNull Z value) {
        ItemMeta m = item.getItemMeta();
        if (m != null) {
            m.getPersistentDataContainer().set(new NamespacedKey(Core.getInstance(), key), dataType, value);
            item.setItemMeta(m);
        }
        return item;
    }

    // --- NBT METHODS (INSTANCES) ---
    public <T, Z> Z getNbt(@NotNull PersistentDataType<T, Z> dataType, @NotNull String key) {
        return this.meta.getPersistentDataContainer().get(new NamespacedKey(Core.getInstance(), key), dataType);
    }

    public <T, Z> @NotNull ItemBuilder addNbt(@NotNull PersistentDataType<T, Z> dataType, @NotNull String key, @NotNull Z value) {
        this.meta.getPersistentDataContainer().set(new NamespacedKey(Core.getInstance(), key), dataType, value);
        return this;
    }

    public @NotNull ItemBuilder removeNbt(@NotNull String key) {
        this.meta.getPersistentDataContainer().remove(new NamespacedKey(Core.getInstance(), key));
        return this;
    }

    // --- USAGES (Modifient la meta locale directement) ---
    public @NotNull ItemBuilder cancelStackUsage(boolean state) {
        return state ? addNbt(PersistentDataType.LONG, "Unstakeable", System.nanoTime()) : removeNbt("Unstakeable");
    }

    public @NotNull ItemBuilder cancelCraftUsage(boolean state) {
        return state ? addNbt(PersistentDataType.BOOLEAN, "Uncraftable", true) : removeNbt("Uncraftable");
    }

    public @NotNull ItemBuilder cancelAnvilUsage(boolean state) {
        return state ? addNbt(PersistentDataType.BOOLEAN, "CancelAnvilUsage", true) : removeNbt("CancelAnvilUsage");
    }

    public @NotNull ItemBuilder cancelDisenchantment(boolean state) {
        return state ? addNbt(PersistentDataType.BOOLEAN, "Undisenchantable", true) : removeNbt("Undisenchantable");
    }

    public @NotNull ItemBuilder cancelEnchantement(boolean state) {
        return state ? addNbt(PersistentDataType.BOOLEAN, "Unenchantable", true) : removeNbt("Unenchantable");
    }

    // --- OTHERS ---
    public @NotNull ItemBuilder applyTexture(@NotNull String texture) {
        final long nanoTime = System.nanoTime();
        // Cas particulier : Utils.applyHeadTexture demande souvent l'ItemStack
        this.itemStack.setItemMeta(this.meta);
        Utils.applyHeadTexture(this.itemStack, texture);
        this.meta = this.itemStack.getItemMeta(); // On récupère la meta modifiée par la texture
        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER, "applyTexture", System.nanoTime() - nanoTime);
        return this;
    }

    public @NotNull ItemBuilder setQuantity(int quantity) {
        this.itemStack.setAmount(quantity);
        return this;
    }

    public @NotNull ItemBuilder setDisplayName(@Nullable Component displayName) {
        if (displayName != null) {
            displayName = Component.text().decoration(TextDecoration.ITALIC, false).append(displayName).build();
        }
        this.meta.displayName(displayName);
        return this;
    }

    public @NotNull ItemBuilder setDisplayName(@Nullable String displayName) {
        this.meta.setDisplayName(displayName);
        return this;
    }

    public @NotNull ItemBuilder setDurability(int durability) {
        if (this.meta instanceof Damageable damageable) {
            damageable.setDamage(this.itemStack.getType().getMaxDurability() - durability);
        }
        return this;
    }

    public @NotNull ItemBuilder setUnbreakable(boolean state) {
        this.meta.setUnbreakable(state);
        return this;
    }

    public @NotNull ItemBuilder setLore(@Nullable Component... lore) {
        if (lore == null) {
            this.meta.lore(null);
        } else {
            this.meta.lore(Arrays.stream(lore).map(line -> line == null ? Component.empty() :
                    line.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)).toList());
        }
        return this;
    }

    public @NotNull ItemBuilder addLore(@Nullable Component... lines) {
        if (lines == null) return this;
        List<Component> currentLore = this.meta.lore();
        if (currentLore == null) currentLore = new ArrayList<>();
        else currentLore = new ArrayList<>(currentLore);

        for (Component line : lines) {
            currentLore.add(line == null ? Component.empty() : line.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        }
        this.meta.lore(currentLore);
        return this;
    }

    public @NotNull ItemBuilder makeGlow() {
        this.meta.addEnchant(Enchantment.LURE, 1, true);
        this.meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public @NotNull ItemBuilder hideAllAttributes() {
        this.meta.addItemFlags(ItemFlag.values());
        return this;
    }

    // --- FINAL BUILD ---
    public @NotNull ItemStack build() {
        this.itemStack.setItemMeta(this.meta);
        return this.itemStack;
    }

    public static @NotNull List<@NotNull Material> getMaterials() {
        return MATERIALS;
    }
}