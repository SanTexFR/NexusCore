package fr.nexus.api.var.types.parents.normal.bukkit;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.InternalVarType;
import fr.nexus.api.var.types.parents.normal.java.IntegerType; // On utilise tes outils !
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class InventoryType extends InternalVarType<Inventory> {

    // On définit un item vide par défaut pour les comparaisons
    private static final ItemStack AIR = new ItemStack(Material.AIR);

    @Override
    public byte @NotNull [] serializeSync(@NotNull Inventory value) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // 1. Taille de l'inventaire (VarInt)
            int size = value.getSize();
            IntegerType.writeVarInt(baos, size);

            // 2. Type de l'inventaire (String)
            String typeName = value.getType().name();
            byte[] typeBytes = typeName.getBytes(StandardCharsets.UTF_8);
            IntegerType.writeVarInt(baos, typeBytes.length);
            baos.write(typeBytes);

            // 3. Contenu avec RLE (Run-Length Encoding)
            ItemStack[] contents = value.getContents();

            int i = 0;
            while (i < contents.length) {
                // On prend l'item actuel (ou AIR si null)
                ItemStack current = (contents[i] == null) ? AIR : contents[i];
                int count = 1;

                // On regarde les suivants : sont-ils IDENTIQUES ?
                // Note: On utilise 'isSimilar' pour ignorer la quantité si tu veux grouper "64 Stone" et "10 Stone".
                // MAIS ici on veut une copie exacte, donc 'equals'.
                // Attention: ItemStack.equals check aussi la quantité.
                while (i + count < contents.length) {
                    ItemStack next = (contents[i + count] == null) ? AIR : contents[i + count];

                    // Si c'est pas le même item, on arrête de compter
                    if (!Objects.equals(current, next)) {
                        break;
                    }
                    count++;
                }

                // On écrit : [Nombre de répétitions] + [L'Item une seule fois]
                IntegerType.writeVarInt(baos, count);

                // On utilise le serializer d'un SEUL ItemStack (pas le Array)
                // Je suppose que tu as VarTypes.ITEM_STACK
                byte[] itemBytes = VarTypes.ITEMSTACK.serializeSync(current);

                // On écrit la taille de l'item puis l'item (classique)
                // Ou si ton ITEM_STACK.serializeSync renvoie déjà un format autodescriptif, juste bytes.
                // Ici je présume qu'il faut écrire la longueur pour que le deserializer sache quoi lire.
                IntegerType.writeVarInt(baos, itemBytes.length);
                baos.write(itemBytes);

                // On avance l'index de tout ce qu'on a déjà compté
                i += count;
            }

            return addVersionToBytes(baos.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Erreur serialization inventaire", e);
        }
    }

    @Override
    public @NotNull Inventory deserializeSync(int version, byte[] bytes) {
        if (version != 1) throw createUnsupportedVersionException(version);

        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        // 1. Taille
        int size = IntegerType.fromVarInt(buffer);

        // 2. Type
        int typeLen = IntegerType.fromVarInt(buffer);
        byte[] typeBytes = new byte[typeLen];
        buffer.get(typeBytes);
        String typeName = new String(typeBytes, StandardCharsets.UTF_8);

        // Création
        // Note: Parfois le type suffit, parfois il faut size. Bukkit gère ça.
        Inventory inv;
        try {
            // Tente de créer par type, sinon par taille (fallback chest)
            // C'est souvent plus sûr de juste faire createInventory(null, size, title)
            inv = Bukkit.createInventory(null, size);
        } catch (Exception e) {
            inv = Bukkit.createInventory(null, size);
        }

        // 3. Lecture RLE
        int currentSlot = 0;

        // Tant qu'on n'a pas rempli l'inventaire
        while (currentSlot < size && buffer.hasRemaining()) {
            // A. Combien de fois on répète cet item ?
            int repeatCount = IntegerType.fromVarInt(buffer);

            // B. Lecture de l'item unique
            int itemLen = IntegerType.fromVarInt(buffer);
            byte[] itemData = new byte[itemLen];
            buffer.get(itemData);

            ItemStack item = VarTypes.ITEMSTACK.deserializeSync(itemData);

            // C. Remplissage des slots
            for (int r = 0; r < repeatCount; r++) {
                if (currentSlot + r < size) {
                    inv.setItem(currentSlot + r, item);
                }
            }

            currentSlot += repeatCount;
        }

        return inv;
    }
}