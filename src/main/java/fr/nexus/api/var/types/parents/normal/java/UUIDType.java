package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.parents.InternalVarType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.UUID;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class UUIDType extends InternalVarType<UUID> {

    @Override
    public byte @NotNull [] serializeSync(@NotNull UUID uuid) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return addVersionToBytes(buffer.array());
    }

    @Override
    public @NotNull UUID deserializeSync(int version, byte[] bytes) {
        if (version == 1) {
            if (bytes.length < 16) {
                throw new IllegalArgumentException("Buffer trop court pour un UUID (" + bytes.length + " octets)");
            }
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            return new UUID(buffer.getLong(), buffer.getLong());
        } else {
            throw createUnsupportedVersionException(version);
        }
    }
}