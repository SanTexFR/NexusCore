package fr.nexus.api.var.types.parents.normal.bukkit;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.normal.VarType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Arrays;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class InventoryType extends VarType<Inventory> {

    private static final @NotNull ItemStack AIR_ITEM_STACK = new ItemStack(Material.AIR);

    public InventoryType() {
        super(Inventory.class, 1);
    }

    @Override
    public byte @NotNull [] serializeSync(@NotNull Inventory value) {
        // Taille réelle de l'inventaire
        int size = value.getSize();

        // Contenu des slots (remplace null par AIR)
        final ItemStack[] contents = value.getContents();
        Arrays.setAll(contents, i -> contents[i] != null ? contents[i] : AIR_ITEM_STACK);
        final byte[] itemBytes = VarTypes.ITEMSTACK_ARRAY.serializeSync(contents);

        // Type de l'inventaire (pour recréer le type si nécessaire)
        final byte[] typeBytes = value.getType().name().getBytes();

        // Allouer le buffer avec la taille stockée
        final ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES // taille
                + Integer.BYTES + typeBytes.length // type
                + itemBytes.length); // contenu
        buffer.putInt(size);
        buffer.putInt(typeBytes.length);
        buffer.put(typeBytes);
        buffer.put(itemBytes);

        return addVersionToBytes(buffer.array());
    }

    @Override
    public @NotNull Inventory deserializeSync(byte @NotNull [] bytes) {
        final VersionAndRemainder var = readVersionAndRemainder(bytes);
        return deserialize(var.version(), var.remainder());
    }

    private @NotNull Inventory deserialize(int version, byte[] bytes) {
        if (version != 1) throw createUnsupportedVersionException(version);

        final ByteBuffer buffer = ByteBuffer.wrap(bytes);

        // Taille de l'inventaire
        int size = buffer.getInt();

        // Type de l'inventaire
        int typeLen = buffer.getInt();
        byte[] typeBytes = new byte[typeLen];
        buffer.get(typeBytes);
        String typeName = new String(typeBytes);

        // Contenu des slots
        byte[] itemBytes = new byte[buffer.remaining()];
        buffer.get(itemBytes);
        final ItemStack[] contents = VarTypes.ITEMSTACK_ARRAY.deserializeSync(itemBytes);

        // Crée l'inventaire avec la bonne taille
        Inventory inv = Bukkit.createInventory(null, size);

        // Copie sécurisée des items
        if (contents != null) {
            int limit = Math.min(contents.length, inv.getSize());
            for (int i = 0; i < limit; i++) {
                inv.setItem(i, contents[i]);
            }
        }

        return inv;
    }
}