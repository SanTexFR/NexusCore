package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.InternalVarType;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class FloatArrayType extends InternalVarType<float[]> {

    @Override
    public byte @NotNull [] serializeSync(float @NotNull [] value) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(5 + value.length * 4);

        // 1. Taille du tableau en VarInt (Gain de 3 octets si length < 128)
        IntegerType.writeVarInt(baos, value.length);

        // 2. Données brutes
        for (float v : value) {
            int bits = Float.floatToIntBits(v);
            baos.write(bits >>> 24);
            baos.write(bits >>> 16);
            baos.write(bits >>> 8);
            baos.write(bits);
        }

        return addVersionToBytes(baos.toByteArray());
    }

    @Override
    public float @NotNull [] deserializeSync(int version, byte[] bytes) {
        if (version != 1) throw createUnsupportedVersionException(version);

        // On utilise un wrapper ByteBuffer (ou on le fait manuellement avec un index)
        // Manuellement ici pour la performance pure (si tu veux)

        int offset = 0;

        // Lecture manuelle VarInt pour le length (réimplémentation locale rapide)
        // Ou tu utilises IntegerType.readVarInt(ByteBuffer.wrap(bytes)) qui est plus simple
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(bytes);
        int length = IntegerType.fromVarInt(buffer);

        float[] result = new float[length];

        for (int i = 0; i < length; i++) {
            // Lecture des 4 octets
            int bits = ((buffer.get() & 0xFF) << 24) |
                    ((buffer.get() & 0xFF) << 16) |
                    ((buffer.get() & 0xFF) << 8)  |
                    ((buffer.get() & 0xFF));
            result[i] = Float.intBitsToFloat(bits);
        }

        return result;
    }
}