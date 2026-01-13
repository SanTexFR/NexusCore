package fr.nexus.api.var.types.parents.normal.bukkit;

import fr.nexus.api.var.types.parents.InternalVarType;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class EnchantmentType extends InternalVarType<Enchantment>{
    private final@NotNull Registry<@NotNull Enchantment>registry=RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);

    @Override
    public byte @NotNull [] serializeSync(@NotNull Enchantment value) {
        NamespacedKey key = value.getKey();
        byte[] keyBytes = key.getKey().getBytes(); // seulement le key string

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + keyBytes.length);
        buffer.putInt(keyBytes.length);
        buffer.put(keyBytes);

        return addVersionToBytes(buffer.array());
    }

    @Override
    public @NotNull Enchantment deserializeSync(int version, byte[] bytes) {
        if (version != 1) throw createUnsupportedVersionException(version);

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int keyLen = buffer.getInt();
        byte[] keyBytes = new byte[keyLen];
        buffer.get(keyBytes);
        String keyString = new String(keyBytes);

        NamespacedKey namespacedKey = NamespacedKey.minecraft(keyString);
        Enchantment enchant = registry.get(namespacedKey);

        if (enchant == null) {
            throw new IllegalArgumentException("Enchantement inconnu: " + keyString);
        }
        return enchant;
    }
}