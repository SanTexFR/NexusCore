package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.InternalVarType;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class DoubleArrayType extends InternalVarType<double[]> {

    @Override
    public byte @NotNull [] serializeSync(double @NotNull [] value) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(5 + value.length * 8);

        // Taille en VarInt
        IntegerType.writeVarInt(baos, value.length);

        // Données
        for (double v : value) {
            long bits = Double.doubleToLongBits(v);
            baos.write((byte)(bits >>> 56));
            baos.write((byte)(bits >>> 48));
            baos.write((byte)(bits >>> 40));
            baos.write((byte)(bits >>> 32));
            baos.write((byte)(bits >>> 24));
            baos.write((byte)(bits >>> 16));
            baos.write((byte)(bits >>> 8));
            baos.write((byte)(bits));
        }

        return addVersionToBytes(baos.toByteArray());
    }

    @Override
    public double @NotNull [] deserializeSync(int version, byte[] bytes) {
        if (version != 1) throw createUnsupportedVersionException(version);

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int length = IntegerType.fromVarInt(buffer);

        double[] result = new double[length];

        for (int i = 0; i < length; i++) {
            // ByteBuffer gère le getLong automatiquement (Big Endian par défaut)
            result[i] = buffer.getLong();
        }

        return result;
    }
}