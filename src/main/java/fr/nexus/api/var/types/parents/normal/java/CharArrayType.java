package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class CharArrayType extends VarType<char[]> {

    public CharArrayType() {
        super(char[].class, 1);
    }

    @Override
    public byte @NotNull [] serializeSync(char @NotNull [] value) {

        int length = value.length;
        byte[] data = new byte[4 + length * 2]; // 4 bytes = length

        // store length first
        data[0] = (byte) (length >>> 24);
        data[1] = (byte) (length >>> 16);
        data[2] = (byte) (length >>> 8);
        data[3] = (byte) (length);

        int pos = 4;

        for (char c : value) {
            data[pos++] = (byte) (c >>> 8);  // high byte
            data[pos++] = (byte) (c);       // low byte
        }

        return addVersionToBytes(data);
    }

    @Override
    public char @NotNull [] deserializeSync(byte @NotNull [] bytes) {
        VersionAndRemainder var = readVersionAndRemainder(bytes);
        return deserialize(var.version(), var.remainder());
    }

    private char @NotNull [] deserialize(int version, byte[] bytes) {
        if (version != 1)
            throw createUnsupportedVersionException(version);

        // read length
        int length =
                ((bytes[0] & 0xFF) << 24) |
                        ((bytes[1] & 0xFF) << 16) |
                        ((bytes[2] & 0xFF) << 8) |
                        (bytes[3] & 0xFF);

        char[] result = new char[length];

        int pos = 4;

        for (int i = 0; i < length; i++) {
            int high = bytes[pos++] & 0xFF;
            int low  = bytes[pos++] & 0xFF;
            result[i] = (char) ((high << 8) | low);
        }

        return result;
    }
}
