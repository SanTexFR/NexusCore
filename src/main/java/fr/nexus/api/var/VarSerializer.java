package fr.nexus.api.var;

import fr.nexus.api.var.types.VarSubType;
import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.Vars;
import fr.nexus.api.var.types.parents.map.MapType;
import fr.nexus.api.var.types.parents.map.MapVarType;
import fr.nexus.api.var.types.parents.VarType;
import fr.nexus.api.var.types.parents.normal.java.IntegerType;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.jpountz.lz4.*;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings({"unused", "UnusedReturnValue", "unchecked"})
class VarSerializer {

    // --- INSTANCES ---
    private static final LZ4Factory factory = LZ4Factory.fastestInstance();
    private static final LZ4Compressor COMPRESSOR = factory.fastCompressor();
    private static final LZ4FastDecompressor DECOMPRESSOR = factory.fastDecompressor();
    private static final ExecutorService LOOM_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private static final String SIG_SEP = "§";

    // --- SAVE OPTIMISÉ V4 (IDENTITY CACHE) ---

    public static byte[] serializeDataSync(@NotNull Object2ObjectOpenHashMap<@NotNull String, @NotNull VarEntry<?>> data) {
        return serializeDataAsync(data).join();
    }

    public static @NotNull CompletableFuture<byte[]> serializeDataAsync(
            @NotNull Object2ObjectOpenHashMap<@NotNull String, @NotNull VarEntry<?>> data) {

        if (data.isEmpty()) return CompletableFuture.completedFuture(new byte[0]);

        return CompletableFuture.supplyAsync(() -> {
            try {
                // On appelle la méthode interne protégée par un try-catch
                return serializeInternal(data);
            } catch (IOException e) {
                // On "wrap" l'erreur pour qu'elle puisse sortir du thread async
                throw new CompletionException(e);
            }
        }, LOOM_EXECUTOR);
    }

    // Méthode extraite pour la clarté et l'application du Double Cache
    private static byte[] serializeInternal(Object2ObjectOpenHashMap<String, VarEntry<?>> data) throws IOException {

        // Cache: Instance de Type -> Context (Buffer + Signature)
        final IdentityHashMap<Vars, TypeContext> contextCache = new IdentityHashMap<>();

        // Map finale pour l'assemblage: Signature -> Context
        final Map<String, TypeContext> signatureGroups = new Object2ObjectOpenHashMap<>();

        for (var entry : data.object2ObjectEntrySet()) {
            VarEntry<?> varEntry = entry.getValue();
            if (!varEntry.persistent()) continue;

            Vars type = varEntry.type();

            // Lookup Instantané
            TypeContext ctx = contextCache.get(type);

            if (ctx == null) {
                // Miss cache : On génère
                String signature = getSignature(type);

                // Est-ce qu'un autre Type avec la même signature existe déjà ?
                ctx = signatureGroups.get(signature);

                if (ctx == null) {
                    ctx = new TypeContext();
                    ctx.buffer = new ByteArrayOutputStream(65536);
                    ctx.sampleType = type;
                    ctx.signature = signature;
                    signatureGroups.put(signature, ctx);
                }

                // On enregistre ce context pour cette instance de Type
                contextCache.put(type, ctx);
            }

            // Écriture (Zéro alloc String, Zéro alloc List)
            writeString(ctx.buffer, entry.getKey());

            // Logic Serialisation...
            byte[] valueBytes;
            if (!type.needAsync()) {
                if (type.isWrapper()) {
                    valueBytes = ((VarSubType<Object>) type).serializeSync(varEntry.value());
                } else {
                    valueBytes = ((MapVarType<Object, Object>) type).serializeSync((Map<Object, Object>) varEntry.value());
                }
            } else {
                if (type.isWrapper()) {
                    valueBytes = ((VarSubType<Object>) type).serializeAsync(varEntry.value()).join();
                } else {
                    valueBytes = ((MapVarType<Object, Object>) type).serializeAsync((Map<Object, Object>) varEntry.value()).join();
                }
            }
            writeByteArray(ctx.buffer, valueBytes);

            // Incrément
            ctx.count++;
        }

        // Assemblage
        final ByteArrayOutputStream mainBaos = new ByteArrayOutputStream(data.size() * 50);

        for (TypeContext ctx : signatureGroups.values()) {
            writeTypeHeader(mainBaos, ctx.sampleType);
            IntegerType.writeVarInt(mainBaos, ctx.count);
            ctx.buffer.writeTo(mainBaos);
        }

        return compress(mainBaos.toByteArray());
    }

    // Petite classe conteneur pour éviter les Maps multiples
    private static class TypeContext {
        ByteArrayOutputStream buffer;
        Vars sampleType;
        String signature;
        int count = 0;
    }

    // --- LOAD (Toujours le même, il est parfait) ---

    public static void deserializeDataSync(byte[] serializedData, @NotNull Object2ObjectOpenHashMap<@NotNull String, @NotNull VarEntry<?>> data) {
        deserializeDataAsync(serializedData, data).join();
    }

    public static @NotNull CompletableFuture<Void> deserializeDataAsync(
            byte[] serializedData,
            @NotNull Object2ObjectOpenHashMap<@NotNull String, @NotNull VarEntry<?>> data) {

        if (serializedData == null || serializedData.length == 0) return CompletableFuture.completedFuture(null);

        return CompletableFuture.runAsync(() -> {
            try {
                final byte[] newData = decompress(serializedData);
                final ByteBuffer buffer = ByteBuffer.wrap(newData);

                while (buffer.hasRemaining()) {
                    byte typeId = buffer.get();
                    Vars groupType;

                    if (typeId == 0) { // WRAPPER
                        String typeStr = readString(buffer);
                        VarSubType<Object> varType = (VarSubType<Object>) VarType.getTypes().get(typeStr);
                        if (varType == null) throw new IOException("VarType inconnu: " + typeStr);
                        groupType = varType;
                    } else { // MAP
                        String mapTypeStr = readString(buffer);
                        String keyTypeStr = readString(buffer);
                        String valueTypeStr = readString(buffer);
                        MapType<?> mapType = MapType.getTypes().get(mapTypeStr);
                        VarSubType<Object> keyType = (VarSubType<Object>) VarType.getTypes().get(keyTypeStr);
                        VarSubType<Object> valueType = (VarSubType<Object>) VarType.getTypes().get(valueTypeStr);
                        groupType = new MapVarType<>(mapType, keyType, valueType);
                    }

                    int count = IntegerType.fromVarInt(buffer);
                    boolean isWrapper = groupType.isWrapper();
                    boolean needAsync = groupType.needAsync();

                    for (int i = 0; i < count; i++) {
                        String key = readString(buffer);
                        byte[] valueByte = readByteArray(buffer);

                        if (!needAsync) {
                            if (isWrapper) {
                                Object value = ((VarSubType<Object>) groupType).deserializeSync(valueByte);
                                if (value != null) data.put(key, new VarEntry<>(value, groupType, true));
                            } else {
                                Map<Object, Object> map = ((MapVarType<Object, Object>) groupType).deserializeSync(valueByte);
                                data.put(key, new VarEntry<>(map, groupType, true));
                            }
                        } else {
                            final Vars finalType = groupType;
                            CompletableFuture<?> future = isWrapper
                                    ? ((VarSubType<Object>) finalType).deserializeAsync(valueByte)
                                    : ((MapVarType<Object, Object>) finalType).deserializeAsync(valueByte);

                            future.thenAccept(val -> {
                                if (val != null) {
                                    synchronized (data) {
                                        data.put(key, new VarEntry<>(val, finalType, true));
                                    }
                                }
                            });
                        }
                    }
                }
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, LOOM_EXECUTOR);
    }

    // --- HELPERS ---

    private static String getSignature(Vars type) {
        if (type.isWrapper()) {
            return "W" + SIG_SEP + type.getStringType();
        } else {
            MapVarType<?, ?> mapVar = (MapVarType<?, ?>) type;
            return "M" + SIG_SEP + mapVar.getVarMapType().getStringType() + SIG_SEP
                    + mapVar.getKeyVarType().getStringType() + SIG_SEP
                    + mapVar.getValueVarType().getStringType();
        }
    }

    private static void writeTypeHeader(OutputStream out, Vars type) throws IOException {
        out.write(type.isWrapper() ? 0 : 1);
        if (type.isWrapper()) {
            writeString(out, type.getStringType());
        } else {
            MapVarType<?, ?> mapVar = (MapVarType<?, ?>) type;
            writeString(out, mapVar.getVarMapType().getStringType());
            writeString(out, mapVar.getKeyVarType().getStringType());
            writeString(out, mapVar.getValueVarType().getStringType());
        }
    }

    private static void writeString(OutputStream out, String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        IntegerType.writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    private static void writeByteArray(OutputStream out, byte[] bytes) throws IOException {
        IntegerType.writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    private static String readString(ByteBuffer buffer) {
        int length = IntegerType.fromVarInt(buffer);
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static byte[] readByteArray(ByteBuffer buffer) {
        int length = IntegerType.fromVarInt(buffer);
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return bytes;
    }

    private static byte[] compress(byte[] input) {
        int maxCompressedLength = COMPRESSOR.maxCompressedLength(input.length);
        byte[] compressed = new byte[maxCompressedLength + 4];
        ByteBuffer.wrap(compressed).putInt(input.length);
        int compressedLength = COMPRESSOR.compress(input, 0, input.length, compressed, 4, maxCompressedLength);
        if (compressedLength + 4 < compressed.length) {
            byte[] trimmed = new byte[compressedLength + 4];
            System.arraycopy(compressed, 0, trimmed, 0, trimmed.length);
            return trimmed;
        }
        return compressed;
    }

    private static byte[] decompress(byte[] compressed) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(compressed);
        int originalLength = buffer.getInt();
        byte[] restored = new byte[originalLength];
        DECOMPRESSOR.decompress(compressed, 4, restored, 0, originalLength);
        return restored;
    }
}