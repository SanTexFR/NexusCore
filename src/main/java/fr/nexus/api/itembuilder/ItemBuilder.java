package fr.nexus.api.itembuilder;

import fr.nexus.Core;
import fr.nexus.system.internal.performanceTracker.PerformanceTracker;
import fr.nexus.utils.Utils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "UnusedReturnValue", "deprecation","UnstableApiUsage"})
public class ItemBuilder {
    // VARIABLES (STATICS)
    private static final @NotNull Random random = new Random();
    private static final List<Material> MATERIALS;

    static {
        MATERIALS = Arrays.stream(Material.values())
                .filter(Material::isItem)
                .toList();
    }

    // VARIABLES (INSTANCES)
    private final @NotNull ItemStack itemStack;

    // CONSTRUCTOR
    private ItemBuilder(@NotNull Material material) {
        this.itemStack = ItemStack.of(material);
    }

    private ItemBuilder(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack.clone();
    }

    // METHODS (STATICS)
    public static @NotNull ItemBuilder createItem(@NotNull Material material) {
        return new ItemBuilder(material);
    }

    public static @NotNull ItemBuilder createItem() {
        return new ItemBuilder(Material.DIRT);
    }

    public static @NotNull ItemBuilder cloneFrom(@NotNull ItemStack other) {
        final long nanoTime = System.nanoTime();
        ItemBuilder builder = new ItemBuilder(other);
        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER, "cloneFrom", System.nanoTime() - nanoTime);
        return builder;
    }

    // HELPER PRIVATE (Pour éviter la duplication de code si besoin de l'ItemMeta)
    private void updateMeta(Consumer<ItemMeta> consumer) {
        ItemMeta meta = this.itemStack.getItemMeta();
        if (meta != null) {
            consumer.accept(meta);
            this.itemStack.setItemMeta(meta);
        }
    }

    // NBT / PDC (On garde le PDC pour les données custom du plugin)
    public static <T, Z> Z getNbt(@NotNull ItemStack item, @NotNull PersistentDataType<T, Z> dataType, @NotNull String key) {
        final long nanoTime = System.nanoTime();
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        Z value = meta.getPersistentDataContainer().get(new NamespacedKey(Core.getInstance(), key), dataType);
        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER, "getNbt", System.nanoTime() - nanoTime);
        return value;
    }

    public <T, Z> Z getNbt(@NotNull PersistentDataType<T, Z> dataType, @NotNull String key) {
        return getNbt(this.itemStack, dataType, key);
    }

    public static <T, Z> @NotNull ItemStack addNbt(@NotNull ItemStack item, @NotNull PersistentDataType<T, Z> dataType, @NotNull String key, @NotNull Z value) {
        final long nanoTime = System.nanoTime();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(new NamespacedKey(Core.getInstance(), key), dataType, value);
            item.setItemMeta(meta);
        }
        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER, "addNbt", System.nanoTime() - nanoTime);
        return item;
    }

    public <T, Z> @NotNull ItemBuilder addNbt(@NotNull PersistentDataType<T, Z> dataType, @NotNull String key, @NotNull Z value) {
        addNbt(this.itemStack, dataType, key, value);
        return this;
    }

    public static @NotNull ItemStack removeNbt(@NotNull ItemStack item, @NotNull String key) {
        final long nanoTime = System.nanoTime();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().remove(new NamespacedKey(Core.getInstance(), key));
            item.setItemMeta(meta);
        }
        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER, "removeNbt", System.nanoTime() - nanoTime);
        return item;
    }

    public @NotNull ItemBuilder removeNbt(@NotNull String key) {
        removeNbt(this.itemStack, key);
        return this;
    }

    // USAGES
    public @NotNull ItemBuilder cancelStackUsage(boolean state) {
        if (state) addNbt(PersistentDataType.LONG, "Unstakeable", System.nanoTime());
        else removeNbt("Unstakeable");
        return this;
    }

    public @NotNull ItemBuilder cancelCraftUsage(boolean state) {
        if (state) addNbt(PersistentDataType.BOOLEAN, "Uncraftable", true);
        else removeNbt("Uncraftable");
        return this;
    }

    public @NotNull ItemBuilder cancelAnvilRename(boolean state) {
        if (state) addNbt(PersistentDataType.BOOLEAN, "CancelAnvilRename", true);
        else removeNbt("CancelAnvilRename");
        return this;
    }

    public @NotNull ItemBuilder cancelAnvilUsage(boolean state) {
        if (state) addNbt(PersistentDataType.BOOLEAN, "CancelAnvilUsage", true);
        else removeNbt("CancelAnvilUsage");
        return this;
    }

    public @NotNull ItemBuilder cancelDisenchantment(boolean state) {
        if (state) addNbt(PersistentDataType.BOOLEAN, "Undisenchantable", true);
        else removeNbt("Undisenchantable");
        return this;
    }

    public @NotNull ItemBuilder cancelEnchantement(boolean state) {
        if (state) addNbt(PersistentDataType.BOOLEAN, "Unenchantable", true);
        else removeNbt("Unenchantable");
        return this;
    }

    // OTHERS (Optimized with Data Components)
    public @NotNull ItemBuilder applyTexture(@NotNull String texture) {
        final long nanoTime = System.nanoTime();
        Utils.applyHeadTexture(this.itemStack, texture);
        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER, "applyTexture", System.nanoTime() - nanoTime);
        return this;
    }

    public @NotNull ItemBuilder setQuantity(int quantity) {
        this.itemStack.setAmount(quantity);
        return this;
    }

    public @NotNull ItemBuilder setDisplayName(@Nullable Component displayName) {
        final long nanoTime = System.nanoTime();
        final Component finalTitle = (displayName != null) ?
                Component.text().decoration(TextDecoration.ITALIC, false).append(displayName).build() : null;

        updateMeta(meta -> meta.displayName(finalTitle));

        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER, "setDisplayName", System.nanoTime() - nanoTime);
        return this;
    }

    public @NotNull ItemBuilder setDisplayName(@Nullable String displayName) {
        final long nanoTime = System.nanoTime();
        updateMeta(meta -> meta.setDisplayName(displayName));
        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER, "setDisplayName", System.nanoTime() - nanoTime);
        return this;
    }

    public @NotNull ItemBuilder setDurability(int durability) {
        final long nanoTime = System.nanoTime();
        Integer maxDamage = this.itemStack.getData(DataComponentTypes.MAX_DAMAGE);
        if (maxDamage != null) {
            int currentDamage = Math.max(0, maxDamage - durability);
            this.itemStack.setData(DataComponentTypes.DAMAGE, currentDamage);
        }
        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER, "setDurability", System.nanoTime() - nanoTime);
        return this;
    }

    public @NotNull ItemBuilder setUnbreakable(boolean state) {
        final long nanoTime = System.nanoTime();
        updateMeta(meta -> meta.setUnbreakable(state));
        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER, "setUnbreakable", System.nanoTime() - nanoTime);
        return this;
    }

    public @NotNull ItemBuilder setLore(@Nullable String... lore) {
        final long nanoTime = System.nanoTime();
        if (lore == null || lore.length == 0) {
            this.itemStack.unsetData(DataComponentTypes.LORE);
        } else {
            ItemLore.Builder loreBuilder = ItemLore.lore();
            for (String line : lore) {
                loreBuilder.addLine(Component.text(line == null ? "" : line).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            }
            this.itemStack.setData(DataComponentTypes.LORE, loreBuilder.build());
        }
        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER, "setLore", System.nanoTime() - nanoTime);
        return this;
    }

    public @NotNull ItemBuilder setLore(@Nullable Component... lore) {
        final long nanoTime = System.nanoTime();
        if (lore == null) {
            this.itemStack.unsetData(DataComponentTypes.LORE);
        } else {
            ItemLore.Builder loreBuilder = ItemLore.lore();
            for (Component line : lore) {
                loreBuilder.addLine(line == null ? Component.empty() :
                        line.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            }
            this.itemStack.setData(DataComponentTypes.LORE, loreBuilder.build());
        }
        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER, "setLore", System.nanoTime() - nanoTime);
        return this;
    }

    public @NotNull ItemBuilder addLore(@Nullable Component... lines) {
        if (lines == null || lines.length == 0) return this;
        final long nanoTime = System.nanoTime();

        ItemLore currentLoreComponent = this.itemStack.getData(DataComponentTypes.LORE);
        ItemLore.Builder loreBuilder = ItemLore.lore();

        // Si la lore existe déjà, on récupère et réinjecte les lignes existantes
        if (currentLoreComponent != null) {
            loreBuilder.lines(currentLoreComponent.lines());
        }

        // On ajoute les nouvelles lignes
        for (Component line : lines) {
            loreBuilder.addLine(line == null ? Component.empty() :
                    line.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        }

        this.itemStack.setData(DataComponentTypes.LORE, loreBuilder.build());

        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER, "addLore", System.nanoTime() - nanoTime);
        return this;
    }

    public @NotNull ItemBuilder addLore(@NotNull List<Component> lines) {
        return addLore(lines.toArray(new Component[0]));
    }

    public @NotNull ItemBuilder clearLore() {
        this.itemStack.unsetData(DataComponentTypes.LORE);
        return this;
    }

    public @NotNull ItemBuilder addEnchant(@NotNull Enchantment enchantment, int level) {
        final long nanoTime = System.nanoTime();
        updateMeta(meta -> meta.addEnchant(enchantment, level, true));
        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER, "addEnchant", System.nanoTime() - nanoTime);
        return this;
    }

    public @NotNull ItemBuilder removeEnchant(@NotNull Enchantment enchantment) {
        final long nanoTime = System.nanoTime();
        updateMeta(meta -> meta.removeEnchant(enchantment));
        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER, "removeEnchant", System.nanoTime() - nanoTime);
        return this;
    }

    public @NotNull ItemBuilder makeGlow() {
        final long nanoTime = System.nanoTime();
        this.itemStack.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER, "makeGlow", System.nanoTime() - nanoTime);
        return this;
    }

    public @NotNull ItemBuilder setCustomModelData(@Nullable Integer modelData) {
        final long nanoTime = System.nanoTime();
        updateMeta(meta -> meta.setCustomModelData(modelData));
        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER, "setCustomModelData", System.nanoTime() - nanoTime);
        return this;
    }

    public @Nullable Integer getCustomModelData() {
        ItemMeta meta = this.itemStack.getItemMeta();
        return (meta != null && meta.hasCustomModelData()) ? meta.getCustomModelData() : null;
    }

    public @NotNull ItemBuilder hideAllAttributes() {
        final long nanoTime = System.nanoTime();
        updateMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER, "hideAllAttributes", System.nanoTime() - nanoTime);
        return this;
    }

    // BUILD
    public @NotNull ItemStack build() {
        return this.itemStack;
    }

    // MATERIALS
    public static @NotNull Material getRandomMaterial() {
        int randomIndex = random.nextInt(MATERIALS.size());
        return MATERIALS.get(randomIndex);
    }

    public static @NotNull List<@NotNull Material> getMaterials() {
        return MATERIALS;
    }
}