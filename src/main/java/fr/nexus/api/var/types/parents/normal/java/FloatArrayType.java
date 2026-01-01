package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.VarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class FloatArrayType extends VarType<float[]> {
    @Override
    public byte @NotNull [] serializeSync(float @NotNull [] value) {

        int length = value.length;

        // Chaque float devient un int → 4 bytes via VarTypes.INTEGER
        // Total = 4 (length) + length * 4
        byte[] data = new byte[4 + length * 4];

        // write length
        data[0] = (byte) (length >>> 24);
        data[1] = (byte) (length >>> 16);
        data[2] = (byte) (length >>> 8);
        data[3] = (byte) (length);

        int pos = 4;

        for (float v : value) {
            int bits = Float.floatToIntBits(v);

            data[pos++] = (byte) (bits >>> 24);
            data[pos++] = (byte) (bits >>> 16);
            data[pos++] = (byte) (bits >>> 8);
            data[pos++] = (byte)  bits;
        }

        return addVersionToBytes(data);
    }

    public float @NotNull [] deserializeSync(int version, byte[] bytes) {
        if (version != 1)
            throw createUnsupportedVersionException(version);

        // read array length
        int length =
                ((bytes[0] & 0xFF) << 24) |
                        ((bytes[1] & 0xFF) << 16) |
                        ((bytes[2] & 0xFF) << 8) |
                        (bytes[3] & 0xFF);

        float[] result = new float[length];

        int pos = 4;

        for (int i = 0; i < length; i++) {

            int bits =
                    ((bytes[pos++] & 0xFF) << 24) |
                            ((bytes[pos++] & 0xFF) << 16) |
                            ((bytes[pos++] & 0xFF) << 8)  |
                            ((bytes[pos++] & 0xFF));

            result[i] = Float.intBitsToFloat(bits);
        }

        return result;
    }
}