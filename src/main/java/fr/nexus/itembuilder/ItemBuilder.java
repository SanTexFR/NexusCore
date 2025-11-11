package fr.nexus.itembuilder;

import fr.nexus.Core;
import fr.nexus.performanceTracker.PerformanceTracker;
import fr.nexus.utils.Utils;
import net.kyori.adventure.text.Component;
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
public class ItemBuilder{
    //VARIABLES (STATICS)
    private static final@NotNull Random random=new Random();

    //VARIABLES (INSTANCES)
    private final@NotNull ItemStack itemStack;
    private@NotNull ItemMeta meta;

    //CONSTRUCTOR
    private ItemBuilder(@NotNull Material material){
        this.itemStack=new ItemStack(material);
        this.meta=this.itemStack.getItemMeta();
    }


    //METHODS (STATICS)
    public static@NotNull ItemBuilder createItem(@NotNull Material material){
        return new ItemBuilder(material);
    }
    public static@NotNull ItemBuilder createItem(){
        return new ItemBuilder(Material.DIRT);
    }
    public static@NotNull ItemBuilder cloneFrom(@NotNull ItemStack other){
        final long nanoTime=System.nanoTime();

        final ItemBuilder builder=createItem(other.getType());
        builder.itemStack.setItemMeta(other.getItemMeta());

        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"cloneFrom",System.nanoTime()-nanoTime);
        return builder;
    }

    //METHOD (INSTANCES)

    //NBT
    public static<T,Z>Z getNbt(@NotNull ItemStack item,@NotNull PersistentDataType<T,Z>dataType,@NotNull String key){
        final long nanoTime=System.nanoTime();

        if(!item.hasItemMeta()){
            PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"getNbt",System.nanoTime()-nanoTime);
            return null;
        }

        final ItemMeta meta=item.getItemMeta();
        if(meta==null){
            PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"getNbt",System.nanoTime()-nanoTime);
            return null;
        }

        final Z value=meta.getPersistentDataContainer().get(new NamespacedKey(Core.getInstance(),key),dataType);

        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"getNbt",System.nanoTime()-nanoTime);
        return value;
    }
    public<T,Z>Z getNbt(@NotNull PersistentDataType<T,Z>dataType,@NotNull String key){
        return getNbt(this.itemStack,dataType,key);
    }

    public static<T,Z>@NotNull ItemStack addNbt(@NotNull ItemStack item,@NotNull PersistentDataType<T,Z>dataType,@NotNull String key,@NotNull Z value){
        final long nanoTime=System.nanoTime();

        final ItemMeta meta=item.getItemMeta();
        if(meta==null){
            PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"addNbt",System.nanoTime()-nanoTime);
            return item;
        }

        meta.getPersistentDataContainer().set(new NamespacedKey(Core.getInstance(),key),dataType,value);

        item.setItemMeta(meta);

        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"addNbt",System.nanoTime()-nanoTime);
        return item;
    }
    public<T,Z>@NotNull ItemBuilder addNbt(@NotNull PersistentDataType<T,Z>dataType,@NotNull String key,@NotNull Z value){
        this.itemStack.setItemMeta(this.meta);
        addNbt(this.itemStack,dataType,key,value);
        this.meta=this.itemStack.getItemMeta();
        return this;
    }

    public static<T,Z>@NotNull ItemStack removeNbt(@NotNull ItemStack item,@NotNull String key){
        final long nanoTime=System.nanoTime();

        final ItemMeta meta=item.getItemMeta();
        if(meta==null){
            PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"removeNbt",System.nanoTime()-nanoTime);
            return item;
        }

        meta.getPersistentDataContainer().remove(new NamespacedKey(Core.getInstance(),key));

        item.setItemMeta(meta);

        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"removeNbt",System.nanoTime()-nanoTime);
        return item;
    }
    public<T,Z>@NotNull ItemBuilder removeNbt(@NotNull String key){
        this.itemStack.setItemMeta(this.meta);
        removeNbt(this.itemStack,key);
        this.meta=this.itemStack.getItemMeta();
        return this;
    }

    //USAGES
    public@NotNull ItemBuilder cancelStackUsage(boolean state){
        if(state)addNbt(this.itemStack,PersistentDataType.LONG,"Unstakeable",System.nanoTime());
        else removeNbt(this.itemStack,"Unstakeable");
        return this;
    }
    public@NotNull ItemBuilder cancelCraftUsage(boolean state){
        if(state)addNbt(this.itemStack,PersistentDataType.BOOLEAN,"Uncraftable",true);
        else removeNbt(this.itemStack,"Uncraftable");
        return this;
    }
    public@NotNull ItemBuilder cancelAnvilUsage(boolean state){
        if(state)addNbt(this.itemStack,PersistentDataType.BOOLEAN,"CancelAnvilUsage",true);
        else removeNbt(this.itemStack,"CancelAnvilUsage");
        return this;
    }
    public@NotNull ItemBuilder cancelDisenchantment(boolean state){
        if(state)addNbt(this.itemStack,PersistentDataType.BOOLEAN,"Undisenchantable",true);
        else removeNbt(this.itemStack,"Undisenchantable");
        return this;
    }
    public@NotNull ItemBuilder cancelEnchantement(boolean state){
        if(state)addNbt(this.itemStack,PersistentDataType.BOOLEAN,"Unenchantable",true);
        else removeNbt(this.itemStack,"Unenchantable");
        return this;
    }

    //OTHERS
    public@NotNull ItemBuilder applyTexture(@NotNull String texture){
        final long nanoTime=System.nanoTime();

        this.itemStack.setItemMeta(this.meta);
        Utils.applyHeadTexture(this.itemStack,texture);
        this.meta=this.itemStack.getItemMeta();

        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"applyTexture",System.nanoTime()-nanoTime);
        return this;
    }
    public@NotNull ItemBuilder setQuantity(int quantity){
        final long nanoTime=System.nanoTime();

        this.itemStack.setAmount(quantity);

        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"setQuantity",System.nanoTime()-nanoTime);
        return this;
    }

    public@NotNull ItemBuilder setDisplayName(@Nullable Component displayName){
        final long nanoTime=System.nanoTime();

        this.meta.displayName(displayName);

        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"setDisplayName",System.nanoTime()-nanoTime);
        return this;
    }
    public@NotNull ItemBuilder setDisplayName(@Nullable String displayName){
        final long nanoTime=System.nanoTime();

        this.meta.setDisplayName(displayName);

        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"setDisplayName",System.nanoTime()-nanoTime);
        return this;
    }

    public@NotNull ItemBuilder setDurability(int durability){
        final long nanoTime=System.nanoTime();

        if(this.itemStack.getItemMeta()instanceof Damageable damageable)
            damageable.setDamage(this.itemStack.getType().getMaxDurability()-durability);

        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"setDurability",System.nanoTime()-nanoTime);
        return this;
    }
    public@NotNull ItemBuilder setUnbreakable(boolean state){
        final long nanoTime=System.nanoTime();

        this.itemStack.getItemMeta().setUnbreakable(state);

        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"setUnbreakable",System.nanoTime()-nanoTime);
        return this;
    }

    public@NotNull ItemBuilder setLore(@Nullable String...lore){
        final long nanoTime=System.nanoTime();

        this.meta.setLore(Arrays.asList(lore));

        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"setLore",System.nanoTime()-nanoTime);
        return this;
    }
    public@NotNull ItemBuilder setLore(@Nullable Component...lore){
        final long nanoTime=System.nanoTime();

        this.meta.lore(Arrays.stream(lore).toList());

        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"setLore",System.nanoTime()-nanoTime);
        return this;
    }

    public@NotNull ItemBuilder addEnchant(@NotNull Enchantment enchantment,int level){
        final long nanoTime=System.nanoTime();

        this.meta.addEnchant(enchantment,level,true);

        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"addEnchant",System.nanoTime()-nanoTime);
        return this;
    }
    public@NotNull ItemBuilder removeEnchant(@NotNull Enchantment enchantment){
        final long nanoTime=System.nanoTime();

        this.meta.removeEnchant(enchantment);

        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"removeEnchant",System.nanoTime()-nanoTime);
        return this;
    }

    public@NotNull ItemBuilder makeGlow(){
        final long nanoTime=System.nanoTime();

        this.meta.addEnchant(Enchantment.LURE,1,true);
        this.meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"makeGlow",System.nanoTime()-nanoTime);
        return this;
    }

    public@NotNull ItemBuilder setCustomModelData(@Nullable Integer modelData){
        final long nanoTime=System.nanoTime();

        this.meta.setCustomModelData(modelData);

        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"setCustomModelData",System.nanoTime()-nanoTime);
        return this;
    }
    public@Nullable Integer getCustomModelData(){
        final long nanoTime=System.nanoTime();

        if(this.meta.hasCustomModelData()){
            PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"getCustomModelData",System.nanoTime()-nanoTime);
            return this.meta.getCustomModelData();
        }

        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"getCustomModelData",System.nanoTime()-nanoTime);
        return null;
    }

    public@NotNull ItemBuilder hideAllAttributes(){
        final long nanoTime=System.nanoTime();

        this.meta.addItemFlags(ItemFlag.values());

        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"hideAllAttributes",System.nanoTime()-nanoTime);
        return this;
    }

    //BUILD
    public@NotNull ItemStack build(){
        this.itemStack.setItemMeta(this.meta);
        return this.itemStack;
    }

    //MATERIALS
    public static@NotNull Material getRandomMaterial(){
        final long nanoTime=System.nanoTime();

        final List<ItemStack>itemStacks=Arrays.stream(Material.values())
                .filter(material->material.isItem()&&!material.isAir())
                .map(ItemStack::new)
                .toList();

        int randomIndex=random.nextInt(itemStacks.size());
        final Material material=itemStacks.get(randomIndex).getType();

        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"getRandomMaterial",System.nanoTime()-nanoTime);
        return material;
    }
    public static@NotNull List<@NotNull Material>getMaterials(){
        final long nanoTime=System.nanoTime();

        final List<Material>materials=Arrays.stream(Material.values())
                .filter(Material::isItem)
                .toList();

        PerformanceTracker.increment(PerformanceTracker.Types.ITEM_BUILDER,"getMaterials",System.nanoTime()-nanoTime);
        return materials;
    }
}