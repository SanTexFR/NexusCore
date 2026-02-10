package fr.nexus.api.var.types.parents.map;

import fr.nexus.api.var.types.VarSubType;
import fr.nexus.api.var.types.VarVersion;
import fr.nexus.api.var.types.parents.Vars;
import fr.nexus.utils.CollectionUtils;
import fr.nexus.api.var.types.parents.normal.java.IntegerType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"unused","UnusedReturnValue","unchecked"})
public class MapVarType<T,T2> extends VarVersion implements Vars, CollectionUtils {

    private static final int VERSION = 1;

    private final @NotNull MapType<?> varMapType;
    private final @NotNull VarSubType<T> keyVarType;
    private final @NotNull VarSubType<T2> valueVarType;

    public MapVarType(@NotNull MapType<?> varMapType,
                      @NotNull VarSubType<T> keyVarType,
                      @NotNull VarSubType<T2> valueVarType) {
        super(VERSION);
        this.varMapType = varMapType;
        this.keyVarType = keyVarType;
        this.valueVarType = valueVarType;
    }

    public @NotNull MapType<?> getVarMapType() { return this.varMapType; }
    public @NotNull VarSubType<T> getKeyVarType() { return this.keyVarType; }
    public @NotNull VarSubType<T2> getValueVarType() { return this.valueVarType; }
    public boolean isMap() { return true; }
    public @NotNull String getStringType() {
        return "M<" + this.varMapType.getStringType() + "¦" +
                this.keyVarType.getStringType() + "¦" +
                this.valueVarType.getStringType() + ">";
    }

    // ----------------------------
    // SERIALIZATION
    // ----------------------------

    // SYNC
    public byte[] serializeSync(@NotNull Map<T, T2> map) {
        int totalSize = IntegerType.toVarInt(map.size()).length; // taille mapVar
        List<byte[]> keyBytesList = new ArrayList<>(map.size());
        List<byte[]> valueBytesList = new ArrayList<>(map.size());

        for (Map.Entry<T, T2> entry : map.entrySet()) {
            byte[] kBytes = keyVarType.serializeSync(entry.getKey());
            byte[] vBytes = valueVarType.serializeSync(entry.getValue());
            keyBytesList.add(kBytes);
            valueBytesList.add(vBytes);

            totalSize += IntegerType.toVarInt(kBytes.length).length + kBytes.length;
            totalSize += IntegerType.toVarInt(vBytes.length).length + vBytes.length;
        }

        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        buffer.put(IntegerType.toVarInt(map.size()));

        for (int i = 0; i < map.size(); i++) {
            byte[] kBytes = keyBytesList.get(i);
            byte[] vBytes = valueBytesList.get(i);

            buffer.put(IntegerType.toVarInt(kBytes.length));
            buffer.put(kBytes);

            buffer.put(IntegerType.toVarInt(vBytes.length));
            buffer.put(vBytes);
        }

        return addVersionToBytes(buffer.array());
    }

    public @NotNull Map<T, T2> deserializeSync(byte[] bytes) {
        VersionAndRemainder vr = readVersionAndRemainder(bytes);
        return deserializeSync(vr.version(), vr.remainder());
    }

    public @NotNull Map<T, T2> deserializeSync(int version, byte[] bytes) {
        if (version != VERSION) throw createUnsupportedVersionException(version);

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        Map<T, T2> map = (Map<T, T2>) varMapType.getSupplier().get();

        int mapSize = IntegerType.fromVarInt(buffer);
        for (int i = 0; i < mapSize; i++) {
            byte[] kBytes = readVarBytesFromBuffer(buffer);
            byte[] vBytes = readVarBytesFromBuffer(buffer);
            map.put(keyVarType.deserializeSync(kBytes), valueVarType.deserializeSync(vBytes));
        }
        return map;
    }

    // ASYNC
    public CompletableFuture<byte[]> serializeAsync(@NotNull Map<T, T2> map) {
        List<CompletableFuture<Void>> futures = new ArrayList<>(map.size());
        List<byte[]> keyBytesList = new ArrayList<>(Collections.nCopies(map.size(), null));
        List<byte[]> valueBytesList = new ArrayList<>(Collections.nCopies(map.size(), null));

        int index = 0;
        for (Map.Entry<T, T2> entry : map.entrySet()) {
            final int idx = index++;
            CompletableFuture<byte[]> kFuture = keyVarType.serializeAsync(entry.getKey());
            CompletableFuture<byte[]> vFuture = valueVarType.serializeAsync(entry.getValue());

            CompletableFuture<Void> combined = kFuture.thenCombine(vFuture, (kBytes, vBytes) -> {
                keyBytesList.set(idx, kBytes);
                valueBytesList.set(idx, vBytes);
                return null;
            });
            futures.add(combined);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    int totalSize = IntegerType.toVarInt(map.size()).length;
                    for (int i = 0; i < map.size(); i++) {
                        totalSize += IntegerType.toVarInt(keyBytesList.get(i).length).length + keyBytesList.get(i).length;
                        totalSize += IntegerType.toVarInt(valueBytesList.get(i).length).length + valueBytesList.get(i).length;
                    }

                    ByteBuffer buffer = ByteBuffer.allocate(totalSize);
                    buffer.put(IntegerType.toVarInt(map.size()));
                    for (int i = 0; i < map.size(); i++) {
                        buffer.put(IntegerType.toVarInt(keyBytesList.get(i).length));
                        buffer.put(keyBytesList.get(i));

                        buffer.put(IntegerType.toVarInt(valueBytesList.get(i).length));
                        buffer.put(valueBytesList.get(i));
                    }
                    return addVersionToBytes(buffer.array());
                });
    }

    public CompletableFuture<Map<T, T2>> deserializeAsync(byte[] bytes) {
        VersionAndRemainder vr = readVersionAndRemainder(bytes);
        return deserializeAsync(vr.version(), vr.remainder());
    }

    public CompletableFuture<Map<T, T2>> deserializeAsync(int version, byte[] bytes) {
        if (version != VERSION) {
            CompletableFuture<Map<T, T2>> failed = new CompletableFuture<>();
            failed.completeExceptionally(createUnsupportedVersionException(version));
            return failed;
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        Map<T, T2> map = (Map<T, T2>) varMapType.getSupplier().get();
        int mapSize = IntegerType.fromVarInt(buffer);

        List<CompletableFuture<Void>> futures = new ArrayList<>(mapSize);
        for (int i = 0; i < mapSize; i++) {
            byte[] kBytes = readVarBytesFromBuffer(buffer);
            byte[] vBytes = readVarBytesFromBuffer(buffer);

            CompletableFuture<Void> future = keyVarType.deserializeAsync(kBytes)
                    .thenCombine(valueVarType.deserializeAsync(vBytes), (k, v) -> {
                        map.put(k, v);
                        return null;
                    });
            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> map);
    }

    // ----------------------------
    // UTIL - VarInt helpers
    // ----------------------------

    private static byte[] readVarBytesFromBuffer(ByteBuffer buffer) {
        int length = IntegerType.fromVarInt(buffer);
        byte[] data = new byte[length];
        buffer.get(data);
        return data;
    }

    // VERSION
    public int getVersion() { return VERSION; }
}
