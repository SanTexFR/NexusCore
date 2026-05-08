package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.InternalVarType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class IntegerType extends InternalVarType<Integer> {
    @Override
    public byte @NotNull [] serializeSync(@NotNull Integer value) {
        return addVersionToBytes(toVarInt(zigZagEncode(value)));
    }

    @Override
    public @NotNull Integer deserializeSync(int version, byte[] bytes) {
        if (version == 1) {
            // 1. VarInt (décompresse)
            // 2. ZigZag (remet les négatifs)
            return zigZagDecode(readVarIntFromBytes(bytes));
        } else {
            throw createUnsupportedVersionException(version);
        }
    }

    public static int zigZagEncode(int n) {
        return (n << 1) ^ (n >> 31);
    }

    public static int zigZagDecode(int n) {
        return (n >>> 1) ^ -(n & 1);
    }

    public static void writeVarInt(OutputStream out, int value){
        try{
            while ((value & -128) != 0) {
                out.write(value & 127 | 128);
                value >>>= 7;
            }
            out.write(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int fromVarInt(ByteBuffer buffer) {
        int value = 0;
        int position = 0;
        byte currentByte;

        while (true) {
            // --- SÉCURITÉ AJOUTÉE ---
            if (!buffer.hasRemaining()) {
                // Au lieu de crash violemment, on lance une erreur explicite
                // ou on retourne une valeur par défaut selon ton besoin.
                throw new RuntimeException("Fin de buffer inattendue lors de la lecture d'un VarInt (données corrompues)");
            }
            // -------------------------

            currentByte = buffer.get();
            value |= (currentByte & 127) << position;

            if ((currentByte & 128) == 0) break;
            position += 7;

            if (position >= 32) throw new RuntimeException("VarInt trop grand (corrompu ?)");
        }

        return value;
    }

    public static byte[] toVarInt(int value) {
        byte[] buffer = new byte[5]; // Max taille d'un VarInt
        int index = 0;

        while ((value & -128) != 0) {
            buffer[index++] = (byte) ((value & 127) | 128);
            value >>>= 7;
        }
        buffer[index++] = (byte) value;

        return index == 5 ? buffer : Arrays.copyOf(buffer, index);
    }

    public static int readVarIntFromBytes(byte[] bytes) {
        return fromVarInt(ByteBuffer.wrap(bytes));
    }
}