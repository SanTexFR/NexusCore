package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class LongArrayType extends VarType<long[]> {

    public LongArrayType() {
        super(long[].class, 1);
    }

    @Override
    public byte @NotNull [] serializeSync(long @NotNull [] value) {

        int length = value.length;
        byte[] data = new byte[4 + length * 8]; // 4 bytes pour length + 8 bytes par long

        // store length
        data[0] = (byte) (length >>> 24);
        data[1] = (byte) (length >>> 16);
        data[2] = (byte) (length >>> 8);
        data[3] = (byte) (length);

        int pos = 4;

        for (long v : value) {
            for (int i = 0; i < 8; i++) {
                data[pos++] = (byte) (v >>> (8 * (7 - i)));
            }
        }

        return addVersionToBytes(data);
    }

    @Override
    public long @NotNull [] deserializeSync(byte @NotNull [] bytes) {
        VersionAndRemainder var = readVersionAndRemainder(bytes);
        return deserialize(var.version(), var.remainder());
    }

    private long @NotNull [] deserialize(int version, byte[] bytes) {
        if (version != 1)
            throw createUnsupportedVersionException(version);

        int length =
                ((bytes[0] & 0xFF) << 24) |
                        ((bytes[1] & 0xFF) << 16) |
                        ((bytes[2] & 0xFF) << 8) |
                        (bytes[3] & 0xFF);

        long[] result = new long[length];

        int pos = 4;

        for (int i = 0; i < length; i++) {
            long value = 0;
            for (int j = 0; j < 8; j++) {
                value = (value << 8) | (bytes[pos++] & 0xFF);
            }
            result[i] = value;
        }

        return result;
    }
}