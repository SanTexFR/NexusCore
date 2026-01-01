package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.VarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class DoubleArrayType extends VarType<double[]> {
    @Override
    public byte @NotNull [] serializeSync(double @NotNull [] value) {

        int length = value.length;
        byte[] data = new byte[4 + length * 8]; // 4 bytes = length, 8 bytes per double

        // write length
        data[0] = (byte) (length >>> 24);
        data[1] = (byte) (length >>> 16);
        data[2] = (byte) (length >>> 8);
        data[3] = (byte) (length);

        int pos = 4;

        for (double v : value) {
            long bits = Double.doubleToLongBits(v);

            data[pos++] = (byte) (bits >>> 56);
            data[pos++] = (byte) (bits >>> 48);
            data[pos++] = (byte) (bits >>> 40);
            data[pos++] = (byte) (bits >>> 32);
            data[pos++] = (byte) (bits >>> 24);
            data[pos++] = (byte) (bits >>> 16);
            data[pos++] = (byte) (bits >>> 8);
            data[pos++] = (byte) (bits);
        }

        return addVersionToBytes(data);
    }

    public double @NotNull [] deserializeSync(int version, byte[] bytes) {
        if (version != 1)
            throw createUnsupportedVersionException(version);

        // read array length
        int length =
                ((bytes[0] & 0xFF) << 24) |
                        ((bytes[1] & 0xFF) << 16) |
                        ((bytes[2] & 0xFF) << 8) |
                        (bytes[3] & 0xFF);

        double[] result = new double[length];

        int pos = 4;

        for (int i = 0; i < length; i++) {

            long bits =
                    ((long) (bytes[pos++] & 0xFF) << 56) |
                            ((long) (bytes[pos++] & 0xFF) << 48) |
                            ((long) (bytes[pos++] & 0xFF) << 40) |
                            ((long) (bytes[pos++] & 0xFF) << 32) |
                            ((long) (bytes[pos++] & 0xFF) << 24) |
                            ((long) (bytes[pos++] & 0xFF) << 16) |
                            ((long) (bytes[pos++] & 0xFF) << 8)  |
                            ((long) (bytes[pos++] & 0xFF));

            result[i] = Double.longBitsToDouble(bits);
        }

        return result;
    }
}