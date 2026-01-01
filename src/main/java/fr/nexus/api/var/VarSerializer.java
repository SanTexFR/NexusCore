package fr.nexus.api.var;

import fr.nexus.api.var.types.VarSubType;
import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.Vars;
import fr.nexus.api.var.types.parents.map.MapType;
import fr.nexus.api.var.types.parents.map.MapVarType;
import fr.nexus.api.var.types.parents.VarType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.jpountz.lz4.*;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@SuppressWarnings({"unused","UnusedReturnValue","unchecked"})
class VarSerializer {
    private static final @NotNull LZ4Factory factory = LZ4Factory.fastestInstance();
    private static final @NotNull LZ4Compressor COMPRESSOR = factory.fastCompressor();
    private static final @NotNull LZ4FastDecompressor DECOMPRESSOR = factory.fastDecompressor();

    // SAVE
    public static byte[] serializeDataSync(@NotNull Object2ObjectOpenHashMap<@NotNull String, @NotNull VarEntry<?>> data) {
        return serializeDataAsync(data).join();
    }

    public static @NotNull CompletableFuture<byte[]> serializeDataAsync(
            @NotNull Object2ObjectOpenHashMap<@NotNull String, @NotNull VarEntry<?>> data) {

        if (data.isEmpty()) return CompletableFuture.completedFuture(new byte[]{});

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.max(8192, 200 * data.size()));
        final List<CompletableFuture<Void>> futures = new ArrayList<>();

        data.forEach((key, varEntry) -> {
            if (!varEntry.persistent()) return;

            final byte[] keyBytes = VarTypes.STRING.serializeSync(key);
            final Vars type = varEntry.type();

            if (type.isWrapper()) {
                final byte[] varTypeBytes = VarTypes.STRING.serializeSync(type.getStringType());
                CompletableFuture<byte[]> valueFuture =
                        type.needAsync()
                                ? ((VarSubType<Object>) type).serializeAsync(varEntry.value())
                                : CompletableFuture.completedFuture(((VarSubType<Object>) type).serializeSync(varEntry.value()));

                futures.add(valueFuture.thenAccept(valueBytes -> {
                    synchronized (baos) {
                        try {
                            baos.write(ByteBuffer.allocate(4).putInt(0).array());
                            writeByteArray(baos, keyBytes);
                            writeByteArray(baos, varTypeBytes);
                            writeByteArray(baos, valueBytes);
                        } catch (IOException e) {
                            throw new CompletionException(e);
                        }
                    }
                }));
                return;
            }

            // MAP
            final MapVarType<?, ?> mapVar = (MapVarType<?, ?>) type;
            final byte[] mapTypeBytes = VarTypes.STRING.serializeSync(mapVar.getVarMapType().getStringType());
            final byte[] keyTypeBytes = VarTypes.STRING.serializeSync(mapVar.getKeyVarType().getStringType());
            final byte[] valueTypeBytes = VarTypes.STRING.serializeSync(mapVar.getValueVarType().getStringType());

            CompletableFuture<byte[]> valueFuture =
                    type.needAsync()
                            ? ((MapVarType<Object, Object>) mapVar).serializeAsync((Map<Object, Object>) varEntry.value())
                            : CompletableFuture.completedFuture(((MapVarType<Object, Object>) mapVar).serializeSync((Map<Object, Object>) varEntry.value()));

            futures.add(valueFuture.thenAccept(valueBytes -> {
                synchronized (baos) {
                    try {
                        baos.write(ByteBuffer.allocate(4).putInt(1).array());
                        writeByteArray(baos, keyBytes);
                        writeByteArray(baos, mapTypeBytes);
                        writeByteArray(baos, keyTypeBytes);
                        writeByteArray(baos, valueTypeBytes);
                        writeByteArray(baos, valueBytes);
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }
                }
            }));
        });

        // attend tous les futurs puis compresse une seule fois
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> compress(baos.toByteArray()));
    }

    // COMPRESSION / DECOMPRESSION
    private static byte[] compress(byte[] input) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             LZ4BlockOutputStream lz4Out = new LZ4BlockOutputStream(baos, 128 * 1024, COMPRESSOR)) {

            // taille originale
            baos.write(ByteBuffer.allocate(4).putInt(input.length).array());
            lz4Out.write(input);
            lz4Out.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[]{};
        }
    }

    private static byte[] decompress(byte[] compressed) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
             LZ4BlockInputStream lz4In = new LZ4BlockInputStream(bais, DECOMPRESSOR)) {

            byte[] sizeBytes = new byte[4];
            bais.read(sizeBytes);
            int originalLength = ByteBuffer.wrap(sizeBytes).getInt();

            byte[] decompressed = lz4In.readAllBytes();
            if (decompressed.length != originalLength) throw new IOException("Longueur décompressée incorrecte");
            return decompressed;
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[]{};
        }
    }

    private static void writeByteArray(ByteArrayOutputStream baos, byte[] bytes) throws IOException {
        baos.write(ByteBuffer.allocate(4).putInt(bytes.length).array());
        baos.write(bytes);
    }

    // LOAD
    public static void deserializeDataSync(byte[] serializedData, @NotNull Object2ObjectOpenHashMap<@NotNull String, @NotNull VarEntry<?>> data) {
        deserializeDataAsync(serializedData, data).join();
    }

    public static @NotNull CompletableFuture<Void> deserializeDataAsync(
            byte[] serializedData,
            @NotNull Object2ObjectOpenHashMap<@NotNull String, @NotNull VarEntry<?>> data) {

        if (serializedData.length == 0) return CompletableFuture.completedFuture(null);

        final byte[] newData = decompress(serializedData);

        return CompletableFuture.runAsync(() -> {
            final ByteBuffer buffer = ByteBuffer.wrap(newData);
            final List<CompletableFuture<Void>> futures = new ArrayList<>();

            while (buffer.hasRemaining()) {
                final int type = buffer.getInt();
                final byte[] keyByte = readByteArray(buffer);
                final String key = VarTypes.STRING.deserializeSync(keyByte);

                if (type == 0) {
                    final byte[] varTypeByte = readByteArray(buffer);
                    final String typeStr = VarTypes.STRING.deserializeSync(varTypeByte);
                    final VarSubType<Object> varType = (VarSubType<Object>) VarType.getTypes().get(typeStr);
                    if (varType == null) throw new CompletionException(new IOException("VarType inconnu: " + typeStr));

                    final byte[] valueByte = readByteArray(buffer);
                    CompletableFuture<Object> valueFuture =
                            varType.needAsync()
                                    ? varType.deserializeAsync(valueByte)
                                    : CompletableFuture.completedFuture(varType.deserializeSync(valueByte));

                    futures.add(valueFuture.thenAccept(value -> {
                        if (value == null) throw new CompletionException(new IOException("Valeur null pour la clé: " + key));
                        data.put(key, new VarEntry<>(value, varType, true));
                    }));

                } else {
                    final byte[] mapTypeByte = readByteArray(buffer);
                    final byte[] mapKeyTypeByte = readByteArray(buffer);
                    final byte[] mapValueTypeByte = readByteArray(buffer);
                    final byte[] valueByte = readByteArray(buffer);

                    final String mapTypeStr = VarTypes.STRING.deserializeSync(mapTypeByte);
                    final String keyTypeStr = VarTypes.STRING.deserializeSync(mapKeyTypeByte);
                    final String valueTypeStr = VarTypes.STRING.deserializeSync(mapValueTypeByte);

                    final MapType<?> mapType = MapType.getTypes().get(mapTypeStr);
                    if (mapType == null) throw new CompletionException(new IOException("MapType inconnu: " + mapTypeStr));

                    final VarSubType<Object> keyType = (VarSubType<Object>) VarType.getTypes().get(keyTypeStr);
                    final VarSubType<Object> valueType = (VarSubType<Object>) VarType.getTypes().get(valueTypeStr);
                    if (keyType == null || valueType == null) throw new CompletionException(new IOException("VarType inconnu pour map"));

                    final MapVarType<Object, Object> mapVarType = new MapVarType<>(mapType, keyType, valueType);
                    CompletableFuture<Map<Object, Object>> valueFuture =
                            mapVarType.needAsync()
                                    ? mapVarType.deserializeAsync(valueByte)
                                    : CompletableFuture.completedFuture(mapVarType.deserializeSync(valueByte));

                    futures.add(valueFuture.thenAccept(map -> data.put(key, new VarEntry<>(map, mapVarType, true))));
                }
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }, Var.THREADPOOL);
    }

    private static byte[] readByteArray(@NotNull ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.getInt()];
        buffer.get(bytes);
        return bytes;
    }
}
