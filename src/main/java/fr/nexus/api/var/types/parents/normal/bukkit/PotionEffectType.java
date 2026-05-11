package fr.nexus.api.var.types.parents.normal.bukkit;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.InternalVarType;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class PotionEffectType extends InternalVarType<PotionEffect> {
    private static final@NotNull Registry<org.bukkit.potion.@NotNull PotionEffectType>POTION_REGISTRY=RegistryAccess.registryAccess().getRegistry(RegistryKey.MOB_EFFECT);
    // SYNC
    public byte @NotNull [] serializeSync(@NotNull PotionEffect value) {
        final byte[] typeBytes =
                VarTypes.STRING.serializeSync(value.getType().getKey().toString());

        final ByteBuffer buffer = ByteBuffer.allocate(
                Integer.BYTES + typeBytes.length + // type length + type
                        Integer.BYTES +                    // duration
                        Byte.BYTES +                       // amplifier
                        Byte.BYTES                         // flags
        );

        // TYPE
        buffer.putInt(typeBytes.length);
        buffer.put(typeBytes);

        // DURATION
        buffer.putInt(value.getDuration());

        // AMPLIFIER
        buffer.put((byte) value.getAmplifier());

        // FLAGS (bitmask)
        byte flags = 0;
        if (value.isAmbient())   flags |= 0b001;
        if (value.hasParticles())flags |= 0b010;
        if (value.hasIcon())     flags |= 0b100;

        buffer.put(flags);

        return addVersionToBytes(buffer.array());
    }

    public @NotNull PotionEffect deserializeSync(int version, byte[] bytes) {
        if (version != 1) throw createUnsupportedVersionException(version);

        final ByteBuffer buffer = ByteBuffer.wrap(bytes);

        // TYPE
        final byte[] typeBytes = new byte[buffer.getInt()];
        buffer.get(typeBytes);
        final String keyString = VarTypes.STRING.deserializeSync(typeBytes);

        // DURATION
        final int duration = buffer.getInt();

        // AMPLIFIER
        final int amplifier = buffer.get() & 0xFF;

        // FLAGS
        final byte flags = buffer.get();
        final boolean ambient   = (flags & 0b001) != 0;
        final boolean particles = (flags & 0b010) != 0;
        final boolean icon      = (flags & 0b100) != 0;

        final NamespacedKey key=NamespacedKey.fromString(keyString);
        if(key==null)throw new IllegalArgumentException("Unknown PotionEffectType: " + keyString);

        org.bukkit.potion.PotionEffectType type=POTION_REGISTRY.get(key);


        if(type==null)throw new IllegalArgumentException("Unknown PotionEffectType: " + keyString);

        return new PotionEffect(type, duration, amplifier, ambient, particles, icon);
    }
}