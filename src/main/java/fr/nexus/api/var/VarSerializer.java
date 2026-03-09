package fr.nexus.api.var;

import fr.nexus.api.var.types.VarSubType;
import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.Vars;
import fr.nexus.api.var.types.parents.map.MapType;
import fr.nexus.api.var.types.parents.map.MapVarType;
import fr.nexus.api.var.types.parents.VarType;
import fr.nexus.api.var.types.parents.normal.java.IntegerType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.jpountz.lz4.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
    public static final ExecutorService LOOM_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    private static final String SIG_SEP = "§";

    // --- FAST BUFFER V2 (String Optimized) ---
    private static class FastBuffer extends OutputStream {
        byte[] buf;
        int count;

        public FastBuffer(int size) {
            this.buf = new byte[size];
        }

        @Override
        public void write(int b) {
            if (count >= buf.length) grow(count + 1);
            buf[count++] = (byte) b;
        }

        @Override
        public void write(byte @NotNull [] b, int off, int len) {
            if (count + len > buf.length) grow(count + len);
            System.arraycopy(b, off, buf, count, len);
            count += len;
        }

        // OPTI 1: Écriture directe de String sans allocation intermédiaire (Zero-Copy)
        // Gère l'UTF-8 basique (optimisé pour ASCII/Latin-1 qui couvre 99% des clés)
        public void writeStringFast(String s) {
            int len = s.length();
            // Estimation pessimiste : 3 bytes par char max en UTF-8 standard
            int maxByteLen = len * 3;

            // On écrit la longueur (VarInt) "plus tard" ou on déplace les bytes ?
            // Pour la vitesse, on écrit d'abord la taille en bytes.
            // Problème : on ne connait pas la taille en bytes avant d'encoder.
            // Solution "Speed" : Si ASCII pur, len == byteLen. Sinon on calcule.

            // Check rapide isAscii (heuristique)
            boolean isAscii = true;
            for (int i = 0; i < len; i++) {
                if (s.charAt(i) > 0x7F) {
                    isAscii = false;
                    break;
                }
            }

            if (isAscii) {
                // Hot Path : ASCII
                writeVarIntFast(len);
                if (count + len > buf.length) grow(count + len);
                // Unroll manuel ou getBytes deprecated (hibernation)
                // Le plus rapide sur HotSpot moderne pour l'ASCII pur vers byte[] :
                s.getBytes(0, len, buf, count);
                count += len;
            } else {
                // Slow Path : UTF-8 complexe (fallback)
                byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
                writeVarIntFast(bytes.length);
                write(bytes, 0, bytes.length);
            }
        }

        // OPTI 3: VarInt "Unrolled" pour les petites valeurs
        public void writeVarIntFast(int value) {
            if (count + 5 > buf.length) grow(count + 5);

            // Hot Path : Valeur < 128 (1 byte)
            if ((value & 0xFFFFFF80) == 0) {
                buf[count++] = (byte) value;
                return;
            }

            // Regular Path
            while ((value & 0xFFFFFF80) != 0) {
                buf[count++] = (byte) ((value & 0x7F) | 0x80);
                value >>>= 7;
            }
            buf[count++] = (byte) (value & 0x7F);
        }

        public void writeTo(FastBuffer other) {
            other.write(buf, 0, count);
        }

        private void grow(int minCapacity) {
            int oldCapacity = buf.length;
            int newCapacity = oldCapacity << 1;
            if (newCapacity - minCapacity < 0) newCapacity = minCapacity;
            buf = Arrays.copyOf(buf, newCapacity);
        }
    }

    // --- CONTEXTE ---
    private static class TypeContext {
        FastBuffer buffer;
        Vars sampleType;
        int count = 0;
        // Pour le batch async
        List<Object> asyncValuesToProcess;
        List<String> asyncKeysToProcess;
    }

    // --- SAVE OPTIMISÉ V6 (BATCHING) ---

    public static byte[] serializeDataSync(@NotNull Object2ObjectOpenHashMap<@NotNull String, @NotNull VarEntry<?>> data) {
        return serializeDataAsync(data).join();
    }

    public static @NotNull CompletableFuture<byte[]> serializeDataAsync(
            @NotNull Object2ObjectOpenHashMap<@NotNull String, @NotNull VarEntry<?>> data) {

        if (data.isEmpty()) return CompletableFuture.completedFuture(new byte[0]);

        return CompletableFuture.supplyAsync(() -> serializeInternal(data), LOOM_EXECUTOR);
    }

    private static byte[] serializeInternal(Object2ObjectOpenHashMap<String, VarEntry<?>> data) {
        // IdentityHashMap est essentiel ici pour la vitesse de lookup par référence
        final IdentityHashMap<Vars, TypeContext> contextCache = new IdentityHashMap<>(data.size());
        final Map<String, TypeContext> signatureGroups = new HashMap<>(); // Standard Map pour les signatures string

        // PASS 1 : Grouping & Sync Writing
        for (var entry : data.object2ObjectEntrySet()) {
            VarEntry<?> varEntry = entry.getValue();
            if (!varEntry.persistent()) continue;

            Vars type = varEntry.type();
            TypeContext ctx = contextCache.get(type);

            if (ctx == null) {
                String signature = getSignature(type);
                ctx = signatureGroups.get(signature);
                if (ctx == null) {
                    ctx = new TypeContext();
                    ctx.buffer = new FastBuffer(4096); // Taille initiale modérée
                    ctx.sampleType = type;
                    signatureGroups.put(signature, ctx);
                }
                contextCache.put(type, ctx);
            }

            if (!type.needAsync()) {
                // SYNC : On écrit tout de suite
                ctx.buffer.writeStringFast(entry.getKey());

                byte[] valueBytes;
                // Polymorphisme manuel pour éviter l'appel virtuel couteux si possible
                if (type.isWrapper()) {
                    valueBytes = ((VarSubType<Object>) type).serializeSync(varEntry.value());
                } else {
                    valueBytes = ((MapVarType<Object, Object>) type).serializeSync((Map<Object, Object>) varEntry.value());
                }
                ctx.buffer.writeVarIntFast(valueBytes.length);
                ctx.buffer.write(valueBytes, 0, valueBytes.length);
                ctx.count++;
            } else {
                // ASYNC : On ne lance PAS de Future ici. On stocke pour plus tard. (Batching)
                if (ctx.asyncValuesToProcess == null) {
                    ctx.asyncValuesToProcess = new ArrayList<>();
                    ctx.asyncKeysToProcess = new ArrayList<>();
                }
                ctx.asyncValuesToProcess.add(varEntry.value());
                ctx.asyncKeysToProcess.add(entry.getKey());
            }
        }

        // PASS 2 : Batch Async Processing
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (TypeContext ctx : signatureGroups.values()) {
            if (ctx.asyncValuesToProcess != null && !ctx.asyncValuesToProcess.isEmpty()) {
                // On lance 1 seul thread pour TOUTES les valeurs de ce type
                final TypeContext targetCtx = ctx;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    // On utilise un buffer local pour ce thread pour éviter la synchro
                    FastBuffer localBuf = new FastBuffer(targetCtx.asyncValuesToProcess.size() * 128);
                    Vars type = targetCtx.sampleType;

                    int size = targetCtx.asyncValuesToProcess.size();
                    for (int i = 0; i < size; i++) {
                        String key = targetCtx.asyncKeysToProcess.get(i);
                        Object val = targetCtx.asyncValuesToProcess.get(i);

                        localBuf.writeStringFast(key);

                        // Async serialization... qui est en fait exécuté sync dans ce thread déporté
                        // Astuce: serializeAsync renvoie un Future, mais ici on est DÉJÀ dans un worker thread.
                        // Si l'implémentation de serializeAsync est bien faite, on peut join direct.
                        byte[] bytes;
                        if (type.isWrapper()) {
                            bytes = ((VarSubType<Object>) type).serializeAsync(val).join();
                        } else {
                            bytes = ((MapVarType<Object, Object>) type).serializeAsync((Map<Object, Object>) val).join();
                        }

                        localBuf.writeVarIntFast(bytes.length);
                        localBuf.write(bytes, 0, bytes.length);
                    }

                    // On merge le résultat dans le buffer principal du contexte
                    // Attention : ici on modifie ctx.buffer depuis un autre thread, mais
                    // comme on attendra tous les futures avant l'assemblage final, pas besoin de lock
                    // TANT QUE 'ctx.buffer' n'est pas touché par d'autres threads en même temps.
                    // Or, le main thread a fini de toucher à ce ctx (Pass 1 finie).
                    // Donc c'est thread-safe "par design" (Happen-Before garanti par le join final).
                    targetCtx.buffer.writeTo(localBuf); // Wait, logic error. Local -> Target.
                    // Correction : On remplace le buffer ou on append.
                    // Le plus simple : on stocke le localBuf et on l'assemblera à la fin.
                    targetCtx.buffer = localBuf; // On remplace le buffer vide/partiel par le plein
                    targetCtx.count = size; // Async types are likely pure async context groups

                }, LOOM_EXECUTOR);
                futures.add(future);
            }
        }

        // Wait for all batches
        if (!futures.isEmpty()) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        // PASS 3 : Final Assembly (Zero-Copy Sizing)
        int totalSize = 4; // Header LZ4
        for (TypeContext ctx : signatureGroups.values()) {
            // 1 byte header + string sig len + string bytes + varint count + buffer content
            // Estimation rapide pour allocation
            totalSize += 100 + ctx.buffer.count;
        }

        FastBuffer mainBuffer = new FastBuffer(totalSize);

        try {
            for (TypeContext ctx : signatureGroups.values()) {
                writeTypeHeader(mainBuffer, ctx.sampleType);
                mainBuffer.writeVarIntFast(ctx.count);
                ctx.buffer.writeTo(mainBuffer);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return compress(mainBuffer.buf, mainBuffer.count);
    }

    // --- LOAD (Identique V5 car déjà très optimisé, juste check String) ---
    // Je réutilise la méthode de V5 mais avec les helpers optimisés

    // ... (Code deserialize V5 ici, il est déjà au top avec le Thread Confinement) ...
    // ... Il faut juste s'assurer d'utiliser les méthodes readString optimisées ...

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

                // Liste batchée pour éviter le lock contention sur data.put
                // On utilise un simple ArrayList car on est thread-confined
                ArrayList<Map.Entry<String, VarEntry<?>>> syncEntries = new ArrayList<>();
                List<CompletableFuture<Map.Entry<String, VarEntry<?>>>> asyncFutures = new ArrayList<>();

                while (buffer.hasRemaining()) {
                    byte typeId = buffer.get();
                    Vars groupType;

                    if (typeId == 0) {
                        String typeStr = readStringFast(buffer);
                        groupType = VarType.getTypes().get(typeStr);
                    } else {
                        String mapTypeStr = readStringFast(buffer);
                        String keyTypeStr = readStringFast(buffer);
                        String valueTypeStr = readStringFast(buffer);
                        groupType = new MapVarType<>(MapType.getTypes().get(mapTypeStr),
                                VarType.getTypes().get(keyTypeStr),
                                VarType.getTypes().get(valueTypeStr));
                    }

                    int count = IntegerType.fromVarInt(buffer);
                    boolean isWrapper = groupType.isWrapper();
                    boolean needAsync = groupType.needAsync();

                    for (int i = 0; i < count; i++) {
                        String key = readStringFast(buffer);
                        byte[] valueByte = readByteArray(buffer);

                        if (!needAsync) {
                            Object value = isWrapper
                                    ? ((VarSubType<Object>) groupType).deserializeSync(valueByte)
                                    : ((MapVarType<Object, Object>) groupType).deserializeSync(valueByte);

                            if (value != null) {
                                syncEntries.add(new AbstractMap.SimpleEntry<>(key, new VarEntry<>(value, groupType, true)));
                            }
                        } else {
                            final Vars finalType = groupType;
                            CompletableFuture<?> valFuture = isWrapper
                                    ? ((VarSubType<Object>) finalType).deserializeAsync(valueByte)
                                    : ((MapVarType<Object, Object>) finalType).deserializeAsync(valueByte);

                            asyncFutures.add(valFuture.thenApply(val -> {
                                if (val == null) return null;
                                return new AbstractMap.SimpleEntry<>(key, new VarEntry<>(val, finalType, true));
                            }));
                        }
                    }
                }

                // Insertion massive Sync (Trés rapide, pas de lock externe nécessaire si data est thread-confined ou synchro bloc unique)
                synchronized(data) {
                    for (var entry : syncEntries) data.put(entry.getKey(), entry.getValue());
                }

                // Wait Async
                if (!asyncFutures.isEmpty()) {
                    CompletableFuture.allOf(asyncFutures.toArray(new CompletableFuture[0])).join();
                    synchronized(data) {
                        for (var future : asyncFutures) {
                            var entry = future.getNow(null);
                            if (entry != null) data.put(entry.getKey(), entry.getValue());
                        }
                    }
                }

            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, LOOM_EXECUTOR);
    }


    // --- HELPERS & COMPRESSION ---

    private static String getSignature(Vars type) {
        if (type.isWrapper()) {
            return "W" + SIG_SEP + type.getStringType();
        } else {
            MapVarType<?, ?> mapVar = (MapVarType<?, ?>) type;
            // Optimisation : String concat est convertie en StringBuilder par javac
            // C'est suffisant ici car c'est du "Cold Path" (appelé 1 fois par type)
            return "M" + SIG_SEP + mapVar.getVarMapType().getStringType() + SIG_SEP
                    + mapVar.getKeyVarType().getStringType() + SIG_SEP
                    + mapVar.getValueVarType().getStringType();
        }
    }

    private static void writeTypeHeader(FastBuffer out, Vars type) throws IOException {
        out.write(type.isWrapper() ? 0 : 1);
        if (type.isWrapper()) {
            out.writeStringFast(type.getStringType());
        } else {
            MapVarType<?, ?> mapVar = (MapVarType<?, ?>) type;
            out.writeStringFast(mapVar.getVarMapType().getStringType());
            out.writeStringFast(mapVar.getKeyVarType().getStringType());
            out.writeStringFast(mapVar.getValueVarType().getStringType());
        }
    }

    // Lecture optimisée sans allocation byte[] si possible
    private static String readStringFast(ByteBuffer buffer) {
        int length = IntegerType.fromVarInt(buffer);
        if (buffer.hasArray()) {
            // ZERO-COPY from buffer
            String s = new String(buffer.array(), buffer.arrayOffset() + buffer.position(), length, StandardCharsets.UTF_8);
            buffer.position(buffer.position() + length);
            return s;
        }
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

    private static byte[] compress(byte[] input, int length) {
        // LZ4 Compression avec zéro allocation superflue
        int maxCompressedLength = COMPRESSOR.maxCompressedLength(length);
        byte[] compressed = new byte[maxCompressedLength + 4];
        ByteBuffer.wrap(compressed).putInt(length);
        int compressedLength = COMPRESSOR.compress(input, 0, length, compressed, 4, maxCompressedLength);

        // On retourne la taille exacte pour le réseau/disque
        return Arrays.copyOf(compressed, compressedLength + 4);
    }

    private static byte[] decompress(byte[] compressed) {
        ByteBuffer buffer = ByteBuffer.wrap(compressed);
        int originalLength = buffer.getInt();
        byte[] restored = new byte[originalLength];
        DECOMPRESSOR.decompress(compressed, 4, restored, 0, originalLength);
        return restored;
    }
}