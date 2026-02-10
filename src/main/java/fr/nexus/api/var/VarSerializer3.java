//package fr.nexus.api.var;
//
//import fr.nexus.api.var.types.VarSubType;
//import fr.nexus.api.var.types.parents.VarType;
//import fr.nexus.api.var.types.parents.Vars;
//import fr.nexus.api.var.types.parents.map.MapType;
//import fr.nexus.api.var.types.parents.map.MapVarType;
//import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
//import net.jpountz.lz4.LZ4Compressor;
//import net.jpountz.lz4.LZ4Factory;
//import net.jpountz.lz4.LZ4FastDecompressor;
//import org.jetbrains.annotations.NotNull;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.CompletionException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//@SuppressWarnings({"unused", "UnusedReturnValue", "unchecked"})
//class VarSerializer3 {
//
//    // Optimisation : Instance LZ4 statique
//    private static final @NotNull LZ4Factory factory = LZ4Factory.fastestInstance();
//    private static final @NotNull LZ4Compressor COMPRESSOR = factory.fastCompressor();
//    private static final @NotNull LZ4FastDecompressor DECOMPRESSOR = factory.fastDecompressor();
//
//    // Optimisation Loom : Utilisation de Virtual Threads pour l'I/O intensif
//    // Si tu es en Java 21+. Sinon, garde ton Var.THREADPOOL
//    private static final ExecutorService LOOM_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
//
//    // --- SAVE ---
//
//    public static byte[] serializeDataSync(@NotNull Object2ObjectOpenHashMap<@NotNull String, @NotNull VarEntry<?>> data) {
//        return serializeDataAsync(data).join();
//    }
//
//    public static @NotNull CompletableFuture<byte[]> serializeDataAsync(
//            @NotNull Object2ObjectOpenHashMap<@NotNull String, @NotNull VarEntry<?>> data) {
//
//        // Optimisation Sizing : Si vide, on renvoie direct
//        if (data.isEmpty()) return CompletableFuture.completedFuture(new byte[0]);
//
//        return CompletableFuture.supplyAsync(() -> {
//            // Optimisation Sizing : Taille exacte prévue pour éviter les redimensionnements
//            final List<CompletableFuture<byte[]>> futures = new ArrayList<>(data.size());
//
//            data.forEach((key, varEntry) -> {
//                if (!varEntry.persistent()) return;
//
//                // On lance la sérialisation de chaque entrée en parallèle
//                futures.add(serializeEntry(key, varEntry));
//            });
//
//            // On attend tout le monde
//            // Note: join() est safe ici car on est dans un Virtual Thread
//            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//
//            // Assemblage final (Séquentiel mais très rapide car tout est déjà en byte[])
//            // On estime la taille : nombre d'entrées * 50 octets (moyenne) pour éviter resizing
//            final ByteArrayOutputStream baos = new ByteArrayOutputStream(data.size() * 50);
//
//            try {
//                for (CompletableFuture<byte[]> future : futures) {
//                    baos.write(future.join());
//                }
//                return compress(baos.toByteArray());
//            } catch (IOException e) {
//                throw new CompletionException(e);
//            }
//
//        }, LOOM_EXECUTOR);
//    }
//
//    // Méthode helper pour sérialiser UNE entrée (Key + Value)
//    private static CompletableFuture<byte[]> serializeEntry(String key, VarEntry<?> varEntry) {
//        final Vars type = varEntry.type();
//
//        // Sérialisation de la valeur (peut être async selon le type)
//        CompletableFuture<byte[]> valueBytesFuture;
//
//        if (type.isWrapper()) {
//            valueBytesFuture = type.needAsync()
//                    ? ((VarSubType<Object>) type).serializeAsync(varEntry.value())
//                    : CompletableFuture.completedFuture(((VarSubType<Object>) type).serializeSync(varEntry.value()));
//        } else {
//            MapVarType<Object, Object> mapVar = (MapVarType<Object, Object>) type;
//            valueBytesFuture = type.needAsync()
//                    ? mapVar.serializeAsync((Map<Object, Object>) varEntry.value())
//                    : CompletableFuture.completedFuture(mapVar.serializeSync((Map<Object, Object>) varEntry.value()));
//        }
//
//        return valueBytesFuture.thenApply(valueBytes -> {
//            // Création du buffer pour CETTE entrée uniquement
//            // Taille estimée : Key(10) + Headers(10) + Value(N)
//            ByteArrayOutputStream entryBaos = new ByteArrayOutputStream(20 + valueBytes.length);
//
//            try {
//                // Header Type (0 = Wrapper, 1 = Map) -> écrit sur 1 byte
//                entryBaos.write(type.isWrapper() ? 0 : 1);
//
//                // Écriture de la Clé (VarInt Length + String)
//                writeString(entryBaos, key);
//
//                if (type.isWrapper()) {
//                    // Type Name
//                    writeString(entryBaos, type.getStringType());
//                } else {
//                    // Map details
//                    MapVarType<?, ?> mapVar = (MapVarType<?, ?>) type;
//                    writeString(entryBaos, mapVar.getVarMapType().getStringType());
//                    writeString(entryBaos, mapVar.getKeyVarType().getStringType());
//                    writeString(entryBaos, mapVar.getValueVarType().getStringType());
//                }
//
//                // Payload (Taille VarInt + Bytes)
//                writeByteArray(entryBaos, valueBytes);
//
//                return entryBaos.toByteArray();
//            } catch (IOException e) {
//                throw new CompletionException(e);
//            }
//        });
//    }
//
//    // --- COMPRESSION / DECOMPRESSION ---
//
//    private static byte[] compress(byte[] input) {
//        // Optimisation : Allocation directe optimisée
//        int maxCompressedLength = COMPRESSOR.maxCompressedLength(input.length);
//        byte[] compressed = new byte[maxCompressedLength + 4]; // +4 pour la taille originale
//
//        // On écrit la taille originale dans les 4 premiers octets (Big Endian standard)
//        ByteBuffer.wrap(compressed).putInt(input.length);
//
//        int compressedLength = COMPRESSOR.compress(input, 0, input.length, compressed, 4, maxCompressedLength);
//
//        // On coupe le tableau à la taille réelle (trim)
//        // Note: Arrays.copyOf est très rapide (System.arraycopy)
//        if (compressedLength + 4 < compressed.length) {
//            byte[] trimmed = new byte[compressedLength + 4];
//            System.arraycopy(compressed, 0, trimmed, 0, trimmed.length);
//            return trimmed;
//        }
//        return compressed;
//    }
//
//    private static byte[] decompress(byte[] compressed) throws IOException {
//        ByteBuffer buffer = ByteBuffer.wrap(compressed);
//        int originalLength = buffer.getInt(); // Lit les 4 premiers octets
//
//        // Allocation exacte
//        byte[] restored = new byte[originalLength];
//
//        // Décompression rapide
//        DECOMPRESSOR.decompress(compressed, 4, restored, 0, originalLength);
//        return restored;
//    }
//
//    // --- WRITING UTILS (VarInt Optimization) ---
//
//    // Remplace writeInt + Bytes par VarInt + Bytes
//    private static void writeString(ByteArrayOutputStream out, String s) throws IOException {
//        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
//
//        // APPEL DIRECT : Pas d'allocation, écriture directe dans le stream
//        writeVarInt(out, bytes.length);
//
//        out.write(bytes);
//    }
//
//    private static void writeByteArray(ByteArrayOutputStream out, byte[] bytes) throws IOException {
//        writeVarInt(out, bytes.length);
//        out.write(bytes);
//    }
//
//    // Écriture VarInt (Google Protocol Buffers style)
//    // Stocke un entier sur 1 à 5 octets selon sa valeur.
//    // 0-127 -> 1 octet. 128-16383 -> 2 octets, etc.
//    private static void writeVarInt(ByteArrayOutputStream out, int value) {
//        while ((value & -128) != 0) {
//            out.write(value & 127 | 128);
//            value >>>= 7;
//        }
//        out.write(value);
//    }
//
//    // --- LOAD ---
//
//    public static void deserializeDataSync(byte[] serializedData, @NotNull Object2ObjectOpenHashMap<@NotNull String, @NotNull VarEntry<?>> data) {
//        deserializeDataAsync(serializedData, data).join();
//    }
//
//    public static @NotNull CompletableFuture<Void> deserializeDataAsync(
//            byte[] serializedData,
//            @NotNull Object2ObjectOpenHashMap<@NotNull String, @NotNull VarEntry<?>> data) {
//
//        if (serializedData == null || serializedData.length == 0) return CompletableFuture.completedFuture(null);
//
//        return CompletableFuture.runAsync(() -> {
//            try {
//                final byte[] newData = decompress(serializedData);
//                final ByteBuffer buffer = ByteBuffer.wrap(newData);
//
//                List<CompletableFuture<Void>> futures = new ArrayList<>();
//
//                while (buffer.hasRemaining()) {
//                    // Lecture ID et Clé (Rapide, Main Thread)
//                    byte typeId = buffer.get();
//                    String key = readString(buffer).intern();
//
//                    if (typeId == 0) { // WRAPPER
//                        String typeStr = readString(buffer).intern();
//                        VarSubType<Object> varType = (VarSubType<Object>) VarType.getTypes().get(typeStr);
//                        if (varType == null) throw new IOException("VarType inconnu: " + typeStr);
//
//                        byte[] valueByte = readByteArray(buffer);
//
//                        // --- OPTIMISATION : FAST PATH ---
//                        if (!varType.needAsync()) {
//                            // C'est un type simple (Int, String...), on le fait DIRECTEMENT sans thread
//                            Object value = varType.deserializeSync(valueByte);
//                            if (value != null) {
//                                // Pas besoin de synchronized ici car on est sur le seul thread qui écrit pour l'instant
//                                // (Sauf si d'autres threads async écrivent en même temps, voir note bas de page)
//                                synchronized (data) {
//                                    data.put(key, new VarEntry<>(value, varType, true));
//                                }
//                            }
//                        } else {
//                            // C'est un type lourd (SQL, WebRequest...), on lance un thread
//                            futures.add(varType.deserializeAsync(valueByte).thenAccept(value -> {
//                                if (value != null) {
//                                    synchronized (data) {
//                                        data.put(key, new VarEntry<>(value, varType, true));
//                                    }
//                                }
//                            }));
//                        }
//
//                    } else { // MAP
//                        String mapTypeStr = readString(buffer).intern();
//                        String keyTypeStr = readString(buffer).intern();
//                        String valueTypeStr = readString(buffer).intern();
//
//                        MapType<?> mapType = MapType.getTypes().get(mapTypeStr);
//                        VarSubType<Object> keyType = (VarSubType<Object>) VarType.getTypes().get(keyTypeStr);
//                        VarSubType<Object> valueType = (VarSubType<Object>) VarType.getTypes().get(valueTypeStr);
//
//                        if (mapType == null || keyType == null || valueType == null)
//                            throw new IOException("Types de Map inconnus");
//
//                        MapVarType<Object, Object> mapVarType = new MapVarType<>(mapType, keyType, valueType);
//                        byte[] valueByte = readByteArray(buffer);
//
//                        // --- OPTIMISATION : FAST PATH MAP ---
//                        if (!mapVarType.needAsync()) {
//                            Map<Object, Object> map = mapVarType.deserializeSync(valueByte);
//                            synchronized (data) {
//                                data.put(key, new VarEntry<>(map, mapVarType, true));
//                            }
//                        } else {
//                            futures.add(mapVarType.deserializeAsync(valueByte).thenAccept(map -> {
//                                synchronized (data) {
//                                    data.put(key, new VarEntry<>(map, mapVarType, true));
//                                }
//                            }));
//                        }
//                    }
//                }
//
//                // On attend seulement les (rares) tâches lourdes
//                if (!futures.isEmpty()) {
//                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//                }
//
//            } catch (IOException e) {
//                throw new CompletionException(e);
//            }
//        }, LOOM_EXECUTOR);
//    }
//
//    // --- READING UTILS ---
//
//    private static String readString(ByteBuffer buffer) {
//        // APPEL DIRECT : Lecture optimisée
//        int length = readVarInt(buffer);
//
//        byte[] bytes = new byte[length];
//        buffer.get(bytes);
//        return new String(bytes, StandardCharsets.UTF_8);
//    }
//
//    private static byte[] readByteArray(ByteBuffer buffer) {
//        int length = readVarInt(buffer);
//        byte[] bytes = new byte[length];
//        buffer.get(bytes);
//        return bytes;
//    }
//
//    // Lecture VarInt
//    private static int readVarInt(ByteBuffer buffer) {
//        int value = 0;
//        int position = 0;
//        byte currentByte;
//
//        while (true) {
//            currentByte = buffer.get();
//            value |= (currentByte & 127) << position;
//
//            if ((currentByte & 128) == 0) break;
//            position += 7;
//
//            if (position >= 32) throw new RuntimeException("VarInt trop grand");
//        }
//
//        return value;
//    }
//}