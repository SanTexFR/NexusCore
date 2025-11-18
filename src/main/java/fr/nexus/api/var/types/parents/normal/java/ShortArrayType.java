package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class ShortArrayType extends VarType<short[]> {
    public ShortArrayType() {
        super(short[].class, 1);
    }

    @Override
    public byte @NotNull [] serializeSync(short @NotNull [] value) {

        int length = value.length;
        byte[] data = new byte[4 + length * 2]; // 4 bytes pour length + 2 bytes par short

        // store length
        data[0] = (byte) (length >>> 24);
        data[1] = (byte) (length >>> 16);
        data[2] = (byte) (length >>> 8);
        data[3] = (byte) (length);

        int pos = 4;

        for (short v : value) {
            data[pos++] = (byte) ((v >>> 8) & 0xFF);
            data[pos++] = (byte) (v & 0xFF);
        }

        return addVersionToBytes(data);
    }

    @Override
    public short @NotNull [] deserializeSync(byte @NotNull [] bytes) {
        VersionAndRemainder var = readVersionAndRemainder(bytes);
        return deserialize(var.version(), var.remainder());
    }

    private short @NotNull [] deserialize(int version, byte[] bytes) {
        if (version != 1)
            throw createUnsupportedVersionException(version);

        int length =
                ((bytes[0] & 0xFF) << 24) |
                        ((bytes[1] & 0xFF) << 16) |
                        ((bytes[2] & 0xFF) << 8) |
                        (bytes[3] & 0xFF);

        short[] result = new short[length];

        int pos = 4;

        for (int i = 0; i < length; i++) {
            result[i] = (short) (((bytes[pos++] & 0xFF) << 8) | (bytes[pos++] & 0xFF));
        }

        return result;
    }
}