package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.InternalVarType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class IntegerType extends InternalVarType<Integer> {

    @Override
    public byte @NotNull [] serializeSync(@NotNull Integer value) {
        // On encode en ZigZag puis en VarInt
        return addVersionToBytes(toVarInt(zigZagEncode(value)));
    }

    @Override
    public @NotNull Integer deserializeSync(int version, byte[] bytes) {
        if (version == 1) {
            try {
                return zigZagDecode(readVarIntFromBytes(bytes));
            } catch (Exception e) {
                // Remplace "NexusCore" par le nom de ton plugin
                org.bukkit.Bukkit.getLogger().warning("[NexusCore] Erreur de lecture Integer (Données corrompues). Valeur 0 retournée par sécurité.");
                return 0;
            }
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

    public static int fromVarInt(ByteBuffer buffer) {
        int value = 0;
        int position = 0;
        byte currentByte;

        while (true) {
            if (!buffer.hasRemaining()) {
                // Au lieu de throw une RuntimeException qui fait moche dans les logs
                // On lance une erreur spécifique ou on log proprement.
                throw new IllegalStateException("VarInt incomplet : Fin de buffer");
            }

            currentByte = buffer.get();
            value |= (currentByte & 0x7F) << position;

            if ((currentByte & 0x80) == 0) break;
            position += 7;

            if (position >= 32) throw new RuntimeException("VarInt trop grand (corrompu)");
        }
        return value;
    }

    // Utilise ceci pour écrire proprement dans le flux
    public static void writeVarInt(OutputStream out, int value){
        try{
            while ((value & 0xFFFFFF80) != 0) {
                out.write((value & 0x7F) | 0x80);
                value >>>= 7;
            }
            out.write(value & 0x7F);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static byte[] toVarInt(int value) {
        byte[] buffer = new byte[5];
        int index = 0;
        while ((value & 0xFFFFFF80) != 0) {
            buffer[index++] = (byte) ((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        buffer[index++] = (byte) value;
        return Arrays.copyOf(buffer, index);
    }

    public static int readVarIntFromBytes(byte[] bytes) {
        return fromVarInt(ByteBuffer.wrap(bytes));
    }
}