//package fr.nexus.api.var;
//
//import fr.nexus.api.var.types.VarSubType;
//import fr.nexus.api.var.types.VarTypes;
//import fr.nexus.api.var.types.parents.Vars;
//import fr.nexus.api.var.types.parents.map.java.MapType;
//import fr.nexus.api.var.types.parents.map.java.MapVarType;
//import fr.nexus.api.var.types.parents.VarType;
//import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
//import net.jpountz.lz4.LZ4BlockOutputStream;
//import net.jpountz.lz4.LZ4Compressor;
//import net.jpountz.lz4.LZ4Factory;
//import net.jpountz.lz4.LZ4FastDecompressor;
//import org.jetbrains.annotations.NotNull;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.*;
//
//@SuppressWarnings({"unused","UnusedReturnValue","unchecked"})
//class VarSerializer{
//    private static final@NotNull LZ4Factory factory=LZ4Factory.fastestInstance();
//    private static final@NotNull LZ4Compressor COMPRESSOR=factory.fastCompressor();
//    private static final@NotNull LZ4FastDecompressor DECOMPRESSOR=factory.fastDecompressor();
//
//    //METHODS (STATICS)
//
//    //SAVE
//    public static byte[]serializeDataSync(@NotNull Object2ObjectOpenHashMap<@NotNull String,@NotNull VarEntry<?>>data){
//        return serializeDataAsync(data).join();
//    }
//    public static @NotNull CompletableFuture<byte[]> serializeDataAsync(
//            @NotNull Object2ObjectOpenHashMap<@NotNull String,@NotNull VarEntry<?>>data) {
//
//        if (data.isEmpty()) return CompletableFuture.completedFuture(new byte[]{});
//
//        final List<Map.Entry<String,VarEntry<?>>> entries = new ArrayList<>(data.entrySet());
//        final int chunkSize = (entries.size() + Var.THREAD_AMOUNT - 1) / Var.THREAD_AMOUNT;
//
//        List<CompletableFuture<byte[]>> futures = new ArrayList<>((entries.size() + chunkSize - 1) / chunkSize);
//
//        for (int i = 0; i < entries.size(); i += chunkSize) {
//            final List<Map.Entry<String, VarEntry<?>>> subList = entries.subList(i, Math.min(i + chunkSize, entries.size()));
//
//            CompletableFuture<byte[]> fut = CompletableFuture.supplyAsync(() -> {
//                final ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.max(8192, subList.size() * 256));
//                try {
//                    for (Map.Entry<String, VarEntry<?>> entry : subList) {
//                        final VarEntry<?> varEntry = entry.getValue();
//                        if(!varEntry.persistent())continue;
//
//                        final Vars vars=varEntry.type();
//                        final String key=entry.getKey();
//
//                        byte[] keyBytes = VarTypes.STRING.serializeSync(key);
//
//                        if (vars.isWrapper()) {
//                            byte[] dataTypeBytes, varTypeBytes, valueBytes;
//
//                            dataTypeBytes = VarTypes.STRING.serializeSync("Wrapper");
//                            varTypeBytes = VarTypes.STRING.serializeSync(vars.getStringType());
//
//                            if (vars.needAsync()) {
//                                valueBytes = ((VarSubType<Object>) vars).serializeAsync(varEntry.value()).join();
//                            } else {
//                                valueBytes = ((VarSubType<Object>) vars).serializeSync(varEntry.value());
//                            }
//
//                            writeByteArray(baos, dataTypeBytes);
//                            writeByteArray(baos, keyBytes);
//                            writeByteArray(baos, varTypeBytes);
//                            writeByteArray(baos, valueBytes);
//
//                        } else if (vars.isMap()) {
//                            final MapVarType<?, ?> mapVar = (MapVarType<?, ?>) vars;
//
//                            byte[] dataTypeBytes, mapTypeBytes, keyTypeBytes, valueTypeBytes, valueBytes;
//
//                            dataTypeBytes = VarTypes.STRING.serializeSync("Map");
//                            mapTypeBytes = VarTypes.STRING.serializeSync(mapVar.getVarMapType().getStringType());
//                            keyTypeBytes = VarTypes.STRING.serializeSync(mapVar.getKeyVarType().getStringType());
//                            valueTypeBytes = VarTypes.STRING.serializeSync(mapVar.getValueVarType().getStringType());
//
//                            if (vars.needAsync()) {
//                                valueBytes = ((MapVarType<Object, Object>) mapVar).serializeAsync((Map<Object, Object>) varEntry.value()).join();
//                            } else {
//                                valueBytes = ((MapVarType<Object, Object>) mapVar).serializeSync((Map<Object, Object>) varEntry.value());
//                            }
//
//                            writeByteArray(baos, dataTypeBytes);
//                            writeByteArray(baos, keyBytes);
//                            writeByteArray(baos, mapTypeBytes);
//                            writeByteArray(baos, keyTypeBytes);
//                            writeByteArray(baos, valueTypeBytes);
//                            writeByteArray(baos, valueBytes);
//
//                        } else {
//                            throw new RuntimeException("Type Vars non supporté: " + vars);
//                        }
//                    }
//                } catch (IOException e) {
//                    throw new CompletionException(e);
//                }
//                return baos.toByteArray();
//            }, Var.THREADPOOL);
//
//            futures.add(fut);
//        }
//
//        // combine futures and compress
//        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
//                .thenApply(v -> {
//                    try {
//                        final ByteArrayOutputStream combined = new ByteArrayOutputStream();
//                        for (CompletableFuture<byte[]> f : futures) {
//                            byte[] chunk = f.join();
//                            combined.write(chunk);
//                        }
//
//                        final byte[] allData = combined.toByteArray();
//                        final ByteArrayOutputStream compressedBaos = new ByteArrayOutputStream(allData.length / 2 + 64);
//                        try (LZ4BlockOutputStream lz4Out = new LZ4BlockOutputStream(compressedBaos, 64 * 1024, COMPRESSOR)) {
//                            compressedBaos.write(ByteBuffer.allocate(4).putInt(allData.length).array());
//                            lz4Out.write(allData);
//                        }
//                        return compressedBaos.toByteArray();
//
//                    } catch (IOException e) {
//                        throw new CompletionException(e);
//                    }
//                });
//    }
//
//
//
//
//
//    private static void writeByteArray(ByteArrayOutputStream baos, byte[] bytes) throws IOException {
//        baos.write(ByteBuffer.allocate(4).putInt(bytes.length).array());
//        baos.write(bytes);
//    }
//
//
//
//
//
//
//    //LOAD
//    public static void deserializeDataSync(byte[]serializedData,@NotNull Object2ObjectOpenHashMap<@NotNull String,@NotNull VarEntry<?>>data)throws IOException{
//        deserializeDataAsync(serializedData,data).join();
//    }
//    public static @NotNull CompletableFuture<Void>deserializeDataAsync(
//            byte[] serializedData,
//            @NotNull Object2ObjectOpenHashMap<@NotNull String,@NotNull VarEntry<?>> data) {
//
//        return CompletableFuture.supplyAsync(() -> {
//            data.clear();
//            if (serializedData.length == 0) return List.<BlockData>of();
//
//            final ByteBuffer wrapper = ByteBuffer.wrap(serializedData);
//            final int originalLength = wrapper.getInt();
//
//            final byte[] compressedData = new byte[serializedData.length - Integer.BYTES];
//            wrapper.get(compressedData);
//
//            final ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
//            final ByteBuffer decompressedBuffer;
//            try (net.jpountz.lz4.LZ4BlockInputStream lz4In = new net.jpountz.lz4.LZ4BlockInputStream(bais, DECOMPRESSOR)) {
//                final byte[] decompressed = lz4In.readAllBytes();
//                if (decompressed.length != originalLength)
//                    throw new CompletionException(new IOException("Longueur décompressée incorrecte"));
//                decompressedBuffer = ByteBuffer.wrap(decompressed);
//            } catch (IOException e) {
//                throw new CompletionException(e);
//            }
//
//            List<BlockData> blocks = new ArrayList<>();
//
//            while (decompressedBuffer.hasRemaining()) {
//                byte[] dataTypeBytes = readByteArray(decompressedBuffer);
//                byte[] keyBytes = readByteArray(decompressedBuffer);
//
//                String dataTypeStr = VarTypes.STRING.deserializeSync(dataTypeBytes);
//
//                if ("Wrapper".equals(dataTypeStr)) {
//                    byte[] typeBytes = readByteArray(decompressedBuffer);
//                    byte[] valueBytes = readByteArray(decompressedBuffer);
//                    blocks.add(new BlockData(dataTypeBytes, keyBytes, typeBytes, valueBytes, null, null, null, null));
//                } else if ("Map".equals(dataTypeStr)) {
//                    byte[] mapTypeBytes = readByteArray(decompressedBuffer);
//                    byte[] keyTypeBytes = readByteArray(decompressedBuffer);
//                    byte[] valueTypeBytes = readByteArray(decompressedBuffer);
//                    byte[] mapBytes = readByteArray(decompressedBuffer);
//                    blocks.add(new BlockData(dataTypeBytes, keyBytes, null, null, mapTypeBytes, keyTypeBytes, valueTypeBytes, mapBytes));
//                } else {
//                    throw new CompletionException(new IOException("Type de données inconnu: " + dataTypeStr));
//                }
//            }
//
//            return blocks;
//        }, Var.THREADPOOL).thenCompose(blocks -> {
//            List<CompletableFuture<Void>> futures = new ArrayList<>(blocks.size());
//
//            for (BlockData block : blocks) {
//                final String dataType = VarTypes.STRING.deserializeSync(block.dataTypeBytes);
//                final String key = VarTypes.STRING.deserializeSync(block.keyBytes);
//
//                if ("Wrapper".equals(dataType)) {
//
//                    CompletableFuture<Void> future =
//                            VarTypes.STRING.deserializeAsync(block.typeBytes)
//                                    .thenCompose(typeStr -> {
//                                        VarSubType<Object> varType =
//                                                (VarSubType<Object>) VarType.getTypes().get(typeStr);
//
//                                        if (varType == null)
//                                            throw new CompletionException(
//                                                    new IOException("VarType inconnu: " + typeStr)
//                                            );
//
//                                        CompletableFuture<Object> valueFuture =
//                                                varType.needAsync()
//                                                        ? varType.deserializeAsync(block.valueBytes)
//                                                        : CompletableFuture.completedFuture(
//                                                        varType.deserializeSync(block.valueBytes)
//                                                );
//
//                                        return valueFuture.thenApply(value -> {
//                                            if (value == null)
//                                                throw new CompletionException(
//                                                        new IOException("Valeur null pour la clé: " + key)
//                                                );
//                                            return new TypedValue(varType, value);
//                                        });
//                                    })
//                                    .thenAccept(tv ->
//                                            data.put(key, new VarEntry<>(tv.value(), tv.type(), true))
//                                    );
//
//                    futures.add(future);
//
//                } else if ("Map".equals(dataType)) {
//
//                    CompletableFuture<Void> future =
//                            CompletableFuture.supplyAsync(() -> {
//                                        String mapTypeStr = VarTypes.STRING.deserializeSync(block.mapTypeBytes);
//                                        String keyTypeStr = VarTypes.STRING.deserializeSync(block.keyTypeBytes);
//                                        String valueTypeStr = VarTypes.STRING.deserializeSync(block.valueTypeBytes);
//
//                                        MapType<?> mapType = MapType.getTypes().get(mapTypeStr);
//                                        if (mapType == null)
//                                            throw new CompletionException(
//                                                    new IOException("MapType inconnu: " + mapTypeStr)
//                                            );
//
//                                        VarSubType<Object> keyType =
//                                                (VarSubType<Object>) VarType.getTypes().get(keyTypeStr);
//                                        VarSubType<Object> valueType =
//                                                (VarSubType<Object>) VarType.getTypes().get(valueTypeStr);
//
//                                        if (keyType == null || valueType == null)
//                                            throw new CompletionException(
//                                                    new IOException("VarType inconnu pour map")
//                                            );
//
//                                        return new MapVarType<>(mapType, keyType, valueType);
//                                    }, Var.THREADPOOL)
//                                    .thenCompose(mapVarType ->
//                                            (mapVarType.needAsync()
//                                                    ? mapVarType.deserializeAsync(block.mapBytes)
//                                                    : CompletableFuture.completedFuture(
//                                                    mapVarType.deserializeSync(block.mapBytes)
//                                            ))
//                                                    .thenApply(map ->new TypedValue(mapVarType, map))
//                                    )
//                                    .thenAccept(tv ->
//                                            data.put(key, new VarEntry<>(tv.value(), tv.type(), true))
//                                    );
//
//
//                    futures.add(future);
//
//                } else {
//                    throw new CompletionException(
//                            new IOException("Type de données inconnu: " + dataType)
//                    );
//                }
//            }
//
//            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
//        });
//    }
//
//
//    private static class BlockData {
//        byte[] dataTypeBytes, keyBytes;
//        byte[] typeBytes, valueBytes; // Wrapper
//        byte[] mapTypeBytes, keyTypeBytes, valueTypeBytes, mapBytes; // Map
//
//        public BlockData(byte[] dataTypeBytes, byte[] keyBytes, byte[] typeBytes, byte[] valueBytes,
//                         byte[] mapTypeBytes, byte[] keyTypeBytes, byte[] valueTypeBytes, byte[] mapBytes) {
//            this.dataTypeBytes = dataTypeBytes;
//            this.keyBytes = keyBytes;
//            this.typeBytes = typeBytes;
//            this.valueBytes = valueBytes;
//            this.mapTypeBytes = mapTypeBytes;
//            this.keyTypeBytes = keyTypeBytes;
//            this.valueTypeBytes = valueTypeBytes;
//            this.mapBytes = mapBytes;
//        }
//    }
//
//
//
//
//    private static byte[]readByteArray(@NotNull ByteBuffer buffer) {
//        final byte[]bytes=new byte[buffer.getInt()];
//        buffer.get(bytes);
//        return bytes;
//    }
//    private void putByteArray(@NotNull ByteBuffer buffer,byte[]bytes){
//        buffer.putInt(bytes.length);
//        buffer.put(bytes);
//    }
//
//    record TypedValue(Vars type, Object value) {}
//
//}