package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.InternalVarType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class IntArrayType extends InternalVarType<int[]> {

    @Override
    public byte @NotNull [] serializeSync(int @NotNull [] value) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        // 1. Écriture de la taille du tableau
        writeVarInt(out, value.length);

        // 2. Écriture des éléments avec encodage ZigZag
        for (final int v : value) {
            writeVarInt(out, encodeZigZag(v));
        }

        return addVersionToBytes(out.toByteArray());
    }

    @Override
    public int @NotNull [] deserializeSync(int version, byte[] bytes) {
        if (version != 1)
            throw createUnsupportedVersionException(version);

        // Index = 1 pour sauter l'octet de version
        int index = 1;

        final int[] lenRead = fromVarIntWithOffset(bytes, index);
        final int length = lenRead[0];
        index = lenRead[1];

        final int[] result = new int[length];
        for (int i = 0; i < length; i++) {
            int[] v = fromVarIntWithOffset(bytes, index);
            result[i] = decodeZigZag(v[0]);
            index = v[1];
        }

        return result;
    }

    /**
     * Lit un VarInt standard dans un tableau de bytes à partir d'un offset.
     * @return un tableau int[] de 2 éléments : [0] = la valeur lue, [1] = le nouvel index/offset.
     */
    public static int[] fromVarIntWithOffset(byte[] bytes, int offset) {
        int value = 0;
        int position = 0;
        int index = offset;

        while (index < bytes.length) {
            final byte b = bytes[index++];

            value |= (b & 0x7F) << position;

            if ((b & 0x80) == 0)
                return new int[]{value, index};

            position += 7;

            // 35 bits max pour couvrir 5 octets complets de 7 bits (VarInt 32-bit signés)
            if (position >= 35) {
                throw new RuntimeException("VarInt trop long");
            }
        }

        throw new IllegalArgumentException("VarInt invalide ou tronqué");
    }

    private static void writeVarInt(ByteArrayOutputStream out, int value) {
        while (true) {
            if ((value & ~0x7F) == 0) {
                out.write(value);
                return;
            } else {
                out.write((value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }

    public static int encodeZigZag(int n) {
        return (n << 1) ^ (n >> 31);
    }

    public static int decodeZigZag(int n) {
        return (n >>> 1) ^ -(n & 1);
    }

    public boolean isDefaultOrEmpty(int @Nullable [] value) {
        return value == null || value.length == 0;
    }
}