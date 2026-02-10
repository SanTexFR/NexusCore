package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.InternalVarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class FloatType extends InternalVarType<Float> {

    @Override
    public byte @NotNull [] serializeSync(@NotNull Float value) {
        int bits = Float.floatToIntBits(value);
        // Allocation précise : 4 octets
        byte[] bytes = new byte[] {
                (byte) (bits >>> 24),
                (byte) (bits >>> 16),
                (byte) (bits >>> 8),
                (byte) (bits)
        };
        return addVersionToBytes(bytes);
    }

    @Override
    public @NotNull Float deserializeSync(int version, byte[] bytes) {
        if (version == 1) {
            int bits = ((bytes[0] & 0xFF) << 24) |
                    ((bytes[1] & 0xFF) << 16) |
                    ((bytes[2] & 0xFF) << 8)  |
                    ((bytes[3] & 0xFF));
            return Float.intBitsToFloat(bits);
        } else throw createUnsupportedVersionException(version);
    }
}