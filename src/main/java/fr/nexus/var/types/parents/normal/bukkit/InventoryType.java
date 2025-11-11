package fr.nexus.var.types.parents.normal.bukkit;

import fr.nexus.var.types.VarTypes;
import fr.nexus.var.types.parents.normal.VarType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Arrays;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class InventoryType extends VarType<Inventory>{
    //VARIABLES (STATICS)
    private static final @NotNull ItemStack AIR_ITEM_STACK=new ItemStack(Material.AIR);

    //CONSTRUCTOR
    public InventoryType(){
        super(Inventory.class,1);
    }


    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Inventory value){
        final ItemStack[]contents=value.getContents();
        Arrays.setAll(contents,i->contents[i]!=null?contents[i]:AIR_ITEM_STACK);
        final byte[]bytes=VarTypes.ITEM_STACK_ARRAY.serializeSync(contents);
        final byte[]type=value.getType().name().getBytes();
        final ByteBuffer buffer=ByteBuffer.allocate(Integer.BYTES+type.length+bytes.length);
        buffer.putInt(type.length);
        buffer.put(type);
        buffer.put(bytes);
        return addVersionToBytes(buffer.array());
    }
    public@NotNull Inventory deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull Inventory deserialize(int version, byte[]bytes){
        if(version==1){
            final ByteBuffer buffer=ByteBuffer.wrap(bytes);
            final byte[]typeBytes=new byte[buffer.getInt()];
            buffer.get(typeBytes);
            final String type=new String(typeBytes);
            final byte[]valueBytes=new byte[buffer.remaining()];
            buffer.get(valueBytes);
            final Inventory inv=Bukkit.createInventory(null,org.bukkit.event.inventory.InventoryType.valueOf(type));
            final ItemStack[]contents=VarTypes.ITEM_STACK_ARRAY.deserializeSync(valueBytes);
            if(contents!=null)inv.setContents(contents);
            return inv;
        }else throw createUnsupportedVersionException(version);
    }
}