package fr.nexus.api.itembuilder;

import fr.nexus.api.listeners.Listeners;
import org.bukkit.Material;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class ItemBuilderListeners{
    //VARIABLES (STATICS)
    static{
        Listeners.register(PrepareItemCraftEvent.class,ItemBuilderListeners::onPrepareCraft);
        Listeners.register(CraftItemEvent.class,ItemBuilderListeners::onCraft);
        Listeners.register(PrepareAnvilEvent.class,ItemBuilderListeners::onAnvilPrepare);
        Listeners.register(PrepareItemEnchantEvent.class,ItemBuilderListeners::onPrepareEnchant);
        Listeners.register(EnchantItemEvent.class,ItemBuilderListeners::onEnchantItem);
        Listeners.register(InventoryClickEvent.class,ItemBuilderListeners::onGrindstoneTake);
    }

    //METHODS (STATICS)
    private static void onPrepareEnchant(PrepareItemEnchantEvent event){
        ItemStack item = event.getItem();

        Boolean unnameable = ItemBuilder.getNbt(item, PersistentDataType.BOOLEAN, "Unenchantable");
        if (unnameable != null && unnameable) {
            Arrays.fill(event.getOffers(), null);
            event.setCancelled(true);
        }
    }
    private static void onEnchantItem(EnchantItemEvent event){
        ItemStack item = event.getItem();

        Boolean unnameable = ItemBuilder.getNbt(item, PersistentDataType.BOOLEAN, "Unenchantable");
        if (unnameable != null && unnameable) {
            event.setCancelled(true);
        }
    }

    private static void onGrindstoneTake(InventoryClickEvent e){
        if(!(e.getInventory()instanceof GrindstoneInventory inv))return;
        if(e.getSlotType()!=InventoryType.SlotType.RESULT)return;

        final ItemStack result=inv.getResult();
        if(result==null||result.getType()==Material.AIR)return;

        final Boolean unenchantable=ItemBuilder.getNbt(result, PersistentDataType.BOOLEAN,"Undisenchantable");
        if (unenchantable!=null&&unenchantable)
            e.setCancelled(true);
    }

    private static void onPrepareCraft(PrepareItemCraftEvent e){
        final ItemStack[]items=e.getInventory().getMatrix();
        for(final ItemStack item:items)
            if(item!=null&&canCancelCraft(e.getInventory(),item))
                return;
    }
    private static void onCraft(CraftItemEvent e){
        if(e.isCancelled()) return;

        final ItemStack[]items=e.getInventory().getMatrix();
        for(final ItemStack item:items){
            if(item!=null&&canCancelCraft(e.getInventory(),item)){
                e.setCancelled(true);
                return;
            }
        }
    }

    private static boolean canCancelCraft(@NotNull CraftingInventory inv,@NotNull ItemStack item){
        if(item.getType().equals(Material.AIR))return false;

        final Boolean bool=ItemBuilder.getNbt(item,PersistentDataType.BOOLEAN,"Uncraftable");
        if(bool==null||!bool)return false;

        inv.setResult(new ItemStack(Material.AIR));

        return true;
    }

    //ANVIL
    private static void onAnvilPrepare(PrepareAnvilEvent e) {
        ItemStack firstItem = e.getInventory().getFirstItem();
        ItemStack secondItem = e.getInventory().getSecondItem();

        // 1. Sécurité globale (CancelAnvilUsage)
        final Set<ItemStack> items = new HashSet<>();
        items.add(firstItem);
        items.add(secondItem);

        for (final ItemStack item : items) {
            if (item == null || item.getType().equals(Material.AIR)) continue;

            final Boolean cancelAll = ItemBuilder.getNbt(item, PersistentDataType.BOOLEAN, "CancelAnvilUsage");
            if (cancelAll != null && cancelAll) {
                e.setResult(null);
                return;
            }
        }

        if (firstItem != null && !firstItem.getType().equals(Material.AIR)) {
            final Boolean cancelRename = ItemBuilder.getNbt(firstItem, PersistentDataType.BOOLEAN, "CancelAnvilRename");

            if (cancelRename != null && cancelRename) {
                // C'est ici qu'on utilise la nouvelle API 1.21 via la View
                org.bukkit.inventory.view.AnvilView anvilView = e.getView();
                String renameText = anvilView.getRenameText();

                // On récupère le nom d'origine (on utilise le plain text pour comparer avec getRenameText())
                String originalName = "";
                if (firstItem.hasItemMeta() && firstItem.getItemMeta().hasDisplayName()) {
                    originalName = firstItem.getItemMeta().getDisplayName();
                }

                // Si le texte de l'enclume n'est pas nul et diffère du nom de base
                if (renameText != null && !renameText.equals(originalName)) {
                    e.setResult(null); // On bloque le résultat
                }
            }
        }
    }
}