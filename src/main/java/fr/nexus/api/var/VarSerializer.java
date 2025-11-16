package fr.nexus.api.var;

import fr.nexus.api.var.types.VarSubType;
import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.Vars;
import fr.nexus.api.var.types.parents.map.MapType;
import fr.nexus.api.var.types.parents.map.MapVarType;
import fr.nexus.api.var.types.parents.normal.VarType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;

@SuppressWarnings({"unused","UnusedReturnValue","unchecked"})
class VarSerializer{
    private static final@NotNull LZ4Factory factory=LZ4Factory.fastestInstance();
    private static final@NotNull LZ4Compressor COMPRESSOR=factory.fastCompressor();
    private static final@NotNull LZ4FastDecompressor DECOMPRESSOR=factory.fastDecompressor();

    //METHODS (STATICS)

    //SAVE
    public static byte[]serializeDataSync(@NotNull Object2ObjectOpenHashMap<@NotNull String,@NotNull Object@NotNull[]>data)throws IOException{
        if(data.isEmpty())return null;

        final List<Map.Entry<String,Object[]>>entries=new ArrayList<>(data.entrySet());
        final int chunkSize=(entries.size()+Var.THREAD_AMOUNT-1)/Var.THREAD_AMOUNT;

        final List<CompletableFuture<byte[]>>futures=new ArrayList<>();
        for(int i=0;i<entries.size();i+=chunkSize){
            final List<Map.Entry<String,Object[]>>subList=entries.subList(i,Math.min(i+chunkSize,entries.size()));
            final CompletableFuture<byte[]>future=CompletableFuture.supplyAsync(()->{
                try{
                    final ByteArrayOutputStream baos=new ByteArrayOutputStream();
                    for(final Map.Entry<String,Object[]>entry:subList){
                        final Object[]objects=entry.getValue();
                        final Vars vars=(Vars)objects[1];
                        final byte[]keyBytes=VarTypes.STRING.serializeSync(entry.getKey());

                        if(vars.isWrapper()){
                            final byte[]dataTypeBytes=VarTypes.STRING.serializeSync("Wrapper");
                            final byte[]varTypeBytes=VarTypes.STRING.serializeSync(vars.getStringType());
                            final byte[]valueBytes=((VarSubType<Object>)vars).serializeSync(objects[0]);

                            writeByteArray(baos,dataTypeBytes);
                            writeByteArray(baos,keyBytes);
                            writeByteArray(baos,varTypeBytes);
                            writeByteArray(baos,valueBytes);

                        }else if(vars.isMap()){
                            final byte[]dataTypeBytes=VarTypes.STRING.serializeSync("Map");
                            final byte[]mapTypeBytes=VarTypes.STRING.serializeSync(((MapVarType<?,?>)vars).getVarMapType().getStringType());
                            final byte[]keyTypeBytes=VarTypes.STRING.serializeSync(((MapVarType<?,?>)vars).getKeyVarType().getStringType());
                            final byte[]valueTypeBytes=VarTypes.STRING.serializeSync(((MapVarType<?,?>)vars).getValueVarType().getStringType());
                            final byte[]valueBytes=((MapVarType<Object, Object>) vars).serializeSync((Map<Object,Object>)objects[0]);

                            writeByteArray(baos,dataTypeBytes);
                            writeByteArray(baos,keyBytes);
                            writeByteArray(baos,mapTypeBytes);
                            writeByteArray(baos,keyTypeBytes);
                            writeByteArray(baos,valueTypeBytes);
                            writeByteArray(baos,valueBytes);

                        }else throw new RuntimeException("Type Vars non supporté: " + vars);
                    }
                    return baos.toByteArray();
                }catch(IOException e){
                    throw new RuntimeException(e);
                }
            },Var.THREADPOOL);

            futures.add(future);
        }

        final CompletableFuture<Void>allDone=CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        final CompletableFuture<byte[]>combinedFuture=allDone.thenApply(v->{
            try{
                final ByteArrayOutputStream combinedBaos=new ByteArrayOutputStream();
                for(CompletableFuture<byte[]>f:futures)
                    combinedBaos.write(f.get());
                return combinedBaos.toByteArray();
            }catch(Exception e){
                throw new RuntimeException(e);
            }
        });

        final byte[]allData=combinedFuture.join();
        final ByteArrayOutputStream compressedBaos=new ByteArrayOutputStream();
        try(LZ4BlockOutputStream lz4Out=new LZ4BlockOutputStream(compressedBaos,64*1024, COMPRESSOR)) {
            compressedBaos.write(ByteBuffer.allocate(4).putInt(allData.length).array());
            lz4Out.write(allData);
        }
        return compressedBaos.toByteArray();
    }

    public static @NotNull CompletableFuture<byte[]> serializeDataAsync(
            @NotNull Object2ObjectOpenHashMap<@NotNull String,@NotNull Object@NotNull[]> data) {

        if (data.isEmpty()) return CompletableFuture.completedFuture(new byte[]{});

        final List<Map.Entry<String, Object[]>> entries = new ArrayList<>(data.entrySet());
        final int chunkSize = (entries.size() + Var.THREAD_AMOUNT - 1) / Var.THREAD_AMOUNT;

        List<CompletableFuture<byte[]>> futures = new ArrayList<>((entries.size() + chunkSize - 1) / chunkSize);

        for (int i = 0; i < entries.size(); i += chunkSize) {
            final List<Map.Entry<String, Object[]>> subList = entries.subList(i, Math.min(i + chunkSize, entries.size()));

            CompletableFuture<byte[]> fut = CompletableFuture.supplyAsync(() -> {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.max(8192, subList.size() * 256));
                try {
                    for (Map.Entry<String, Object[]> entry : subList) {
                        final Object[] objects = entry.getValue();
                        final Vars vars = (Vars) objects[1];
                        final String key = entry.getKey();

                        byte[] keyBytes;
                        if (vars.needAsync()) {
                            keyBytes = VarTypes.STRING.serializeAsync(key).join();
                        } else {
                            keyBytes = VarTypes.STRING.serializeSync(key);
                        }

                        if (vars.isWrapper()) {
                            byte[] dataTypeBytes, varTypeBytes, valueBytes;

                            if (vars.needAsync()) {
                                dataTypeBytes = VarTypes.STRING.serializeAsync("Wrapper").join();
                                varTypeBytes = VarTypes.STRING.serializeAsync(vars.getStringType()).join();
                                valueBytes = ((VarSubType<Object>) vars).serializeAsync(objects[0]).join();
                            } else {
                                dataTypeBytes = VarTypes.STRING.serializeSync("Wrapper");
                                varTypeBytes = VarTypes.STRING.serializeSync(vars.getStringType());
                                valueBytes = ((VarSubType<Object>) vars).serializeSync(objects[0]);
                            }

                            writeByteArray(baos, dataTypeBytes);
                            writeByteArray(baos, keyBytes);
                            writeByteArray(baos, varTypeBytes);
                            writeByteArray(baos, valueBytes);

                        } else if (vars.isMap()) {
                            final MapVarType<?, ?> mapVar = (MapVarType<?, ?>) vars;

                            byte[] dataTypeBytes, mapTypeBytes, keyTypeBytes, valueTypeBytes, valueBytes;

                            if (vars.needAsync()) {
                                dataTypeBytes = VarTypes.STRING.serializeAsync("Map").join();
                                mapTypeBytes = VarTypes.STRING.serializeAsync(mapVar.getVarMapType().getStringType()).join();
                                keyTypeBytes = VarTypes.STRING.serializeAsync(mapVar.getKeyVarType().getStringType()).join();
                                valueTypeBytes = VarTypes.STRING.serializeAsync(mapVar.getValueVarType().getStringType()).join();
                                valueBytes = ((MapVarType<Object, Object>) mapVar).serializeAsync((Map<Object, Object>) objects[0]).join();
                            } else {
                                dataTypeBytes = VarTypes.STRING.serializeSync("Map");
                                mapTypeBytes = VarTypes.STRING.serializeSync(mapVar.getVarMapType().getStringType());
                                keyTypeBytes = VarTypes.STRING.serializeSync(mapVar.getKeyVarType().getStringType());
                                valueTypeBytes = VarTypes.STRING.serializeSync(mapVar.getValueVarType().getStringType());
                                valueBytes = ((MapVarType<Object, Object>) mapVar).serializeSync((Map<Object, Object>) objects[0]);
                            }

                            writeByteArray(baos, dataTypeBytes);
                            writeByteArray(baos, keyBytes);
                            writeByteArray(baos, mapTypeBytes);
                            writeByteArray(baos, keyTypeBytes);
                            writeByteArray(baos, valueTypeBytes);
                            writeByteArray(baos, valueBytes);

                        } else {
                            throw new RuntimeException("Type Vars non supporté: " + vars);
                        }
                    }
                } catch (IOException e) {
                    throw new CompletionException(e);
                }
                return baos.toByteArray();
            }, Var.THREADPOOL);

            futures.add(fut);
        }

        // combine futures and compress
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    try {
                        final ByteArrayOutputStream combined = new ByteArrayOutputStream();
                        for (CompletableFuture<byte[]> f : futures) {
                            byte[] chunk = f.join();
                            combined.write(chunk);
                        }

                        final byte[] allData = combined.toByteArray();
                        final ByteArrayOutputStream compressedBaos = new ByteArrayOutputStream(allData.length / 2 + 64);
                        try (LZ4BlockOutputStream lz4Out = new LZ4BlockOutputStream(compressedBaos, 64 * 1024, COMPRESSOR)) {
                            compressedBaos.write(ByteBuffer.allocate(4).putInt(allData.length).array());
                            lz4Out.write(allData);
                        }
                        return compressedBaos.toByteArray();

                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }
                });
    }





    private static void writeByteArray(ByteArrayOutputStream baos, byte[] bytes) throws IOException {
        baos.write(ByteBuffer.allocate(4).putInt(bytes.length).array());
        baos.write(bytes);
    }






    //LOAD
    public static void deserializeDataSync(byte[]serializedData,@NotNull Object2ObjectOpenHashMap<@NotNull String,@NotNull Object@NotNull[]>data)throws IOException{
        data.clear();

        if(serializedData.length==0)return;

        final ByteBuffer wrapper=ByteBuffer.wrap(serializedData);
        final int originalLength=wrapper.getInt();

        final byte[]compressedData=new byte[serializedData.length-Integer.BYTES];
        wrapper.get(compressedData);

        final ByteArrayInputStream bais=new ByteArrayInputStream(compressedData);
        final ByteBuffer decompressedBuffer;
        try(net.jpountz.lz4.LZ4BlockInputStream lz4In=new net.jpountz.lz4.LZ4BlockInputStream(bais,DECOMPRESSOR)){
            final byte[]decompressed=lz4In.readAllBytes();
            if(decompressed.length!=originalLength)
                throw new IOException("Longueur décompressée incorrecte");
            decompressedBuffer=ByteBuffer.wrap(decompressed);
        }

        while(decompressedBuffer.hasRemaining()){
            final byte[]dataTypeBytes=new byte[decompressedBuffer.getInt()];
            decompressedBuffer.get(dataTypeBytes);
            final String dataType=VarTypes.STRING.deserializeSync(dataTypeBytes);

            final byte[]keyBytes=new byte[decompressedBuffer.getInt()];
            decompressedBuffer.get(keyBytes);
            final String key=VarTypes.STRING.deserializeSync(keyBytes);

            if("Wrapper".equals(dataType)){
                final String varTypeString=VarTypes.STRING.deserializeSync(readByteArray(decompressedBuffer));
                final VarSubType<Object>varType=(VarSubType<Object>)VarType.getTypes().get(varTypeString);
                if(varType==null)throw new IOException("VarType inconnu: "+varTypeString);

                final Object value=varType.deserializeSync(readByteArray(decompressedBuffer));
                if(value==null)throw new IOException("Null value");

                data.put(key, new Object[]{value, varType});
            }else if("Map".equals(dataType)){
                final String mapTypeString=VarTypes.STRING.deserializeSync(readByteArray(decompressedBuffer));
                final MapType<?>mapType=MapType.getTypes().get(mapTypeString);
                if(mapType==null)throw new IOException("MapType inconnu: "+mapTypeString);

                final String varTypeString1=VarTypes.STRING.deserializeSync(readByteArray(decompressedBuffer));
                final VarSubType<Object>varType1=(VarSubType<Object>) VarType.getTypes().get(varTypeString1);
                if(varType1==null)throw new IOException("VarType inconnu: "+varTypeString1);

                final String varTypeString2 = VarTypes.STRING.deserializeSync(readByteArray(decompressedBuffer));
                final VarSubType<Object> varType2 = (VarSubType<Object>) VarType.getTypes().get(varTypeString2);
                if(varType2==null)throw new IOException("VarType inconnu: "+varTypeString2);

                final MapVarType<Object,Object>mapVarType=new MapVarType<>(mapType,varType1,varType2);
                data.put(key,new Object[]{mapVarType.deserializeSync(readByteArray(decompressedBuffer)),mapVarType});
            }else throw new IOException("Type de données inconnu: "+dataType);
        }
    }

    public static @NotNull CompletableFuture<Void> deserializeDataAsync(
            byte[] serializedData,
            @NotNull Object2ObjectOpenHashMap<@NotNull String,@NotNull Object@NotNull[]> data) {

        return CompletableFuture.supplyAsync(() -> {
            data.clear();
            if (serializedData.length == 0) return List.<BlockData>of();

            final ByteBuffer wrapper = ByteBuffer.wrap(serializedData);
            final int originalLength = wrapper.getInt();

            final byte[] compressedData = new byte[serializedData.length - Integer.BYTES];
            wrapper.get(compressedData);

            final ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
            final ByteBuffer decompressedBuffer;
            try (net.jpountz.lz4.LZ4BlockInputStream lz4In = new net.jpountz.lz4.LZ4BlockInputStream(bais, DECOMPRESSOR)) {
                final byte[] decompressed = lz4In.readAllBytes();
                if (decompressed.length != originalLength)
                    throw new CompletionException(new IOException("Longueur décompressée incorrecte"));
                decompressedBuffer = ByteBuffer.wrap(decompressed);
            } catch (IOException e) {
                throw new CompletionException(e);
            }

            List<BlockData> blocks = new ArrayList<>();

            while (decompressedBuffer.hasRemaining()) {
                byte[] dataTypeBytes = readByteArray(decompressedBuffer);
                byte[] keyBytes = readByteArray(decompressedBuffer);

                String dataTypeStr = VarTypes.STRING.deserializeSync(dataTypeBytes);

                if ("Wrapper".equals(dataTypeStr)) {
                    byte[] typeBytes = readByteArray(decompressedBuffer);
                    byte[] valueBytes = readByteArray(decompressedBuffer);
                    blocks.add(new BlockData(dataTypeBytes, keyBytes, typeBytes, valueBytes, null, null, null, null));
                } else if ("Map".equals(dataTypeStr)) {
                    byte[] mapTypeBytes = readByteArray(decompressedBuffer);
                    byte[] keyTypeBytes = readByteArray(decompressedBuffer);
                    byte[] valueTypeBytes = readByteArray(decompressedBuffer);
                    byte[] mapBytes = readByteArray(decompressedBuffer);
                    blocks.add(new BlockData(dataTypeBytes, keyBytes, null, null, mapTypeBytes, keyTypeBytes, valueTypeBytes, mapBytes));
                } else {
                    throw new CompletionException(new IOException("Type de données inconnu: " + dataTypeStr));
                }
            }

            return blocks;
        }, Var.THREADPOOL).thenCompose(blocks -> {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (BlockData block : blocks) {
                // Vérification needAsync pour STRING
                boolean keyAsync = VarTypes.STRING.needAsync();
                boolean typeAsync = VarTypes.STRING.needAsync();

                CompletableFuture<String> dataTypeFuture = "Wrapper".equals(VarTypes.STRING.deserializeSync(block.dataTypeBytes))
                        ? CompletableFuture.completedFuture("Wrapper")
                        : keyAsync ? VarTypes.STRING.deserializeAsync(block.dataTypeBytes)
                        : CompletableFuture.completedFuture(VarTypes.STRING.deserializeSync(block.dataTypeBytes));

                CompletableFuture<String> keyFuture = keyAsync
                        ? VarTypes.STRING.deserializeAsync(block.keyBytes)
                        : CompletableFuture.completedFuture(VarTypes.STRING.deserializeSync(block.keyBytes));

                CompletableFuture<Void> blockFuture = dataTypeFuture.thenCombineAsync(keyFuture, (dataType, key) -> {
                    if ("Wrapper".equals(dataType)) {
                        boolean typeAsync2 = VarTypes.STRING.needAsync();
                        CompletableFuture<String> typeFuture = typeAsync2
                                ? VarTypes.STRING.deserializeAsync(block.typeBytes)
                                : CompletableFuture.completedFuture(VarTypes.STRING.deserializeSync(block.typeBytes));

                        return typeFuture.thenCompose(typeStr -> {
                            VarSubType<Object> varType = (VarSubType<Object>) VarType.getTypes().get(typeStr);
                            if (varType == null)
                                throw new CompletionException(new IOException("VarType inconnu: " + typeStr));

                            CompletableFuture<Object> valueFuture = varType.needAsync()
                                    ? varType.deserializeAsync(block.valueBytes)
                                    : CompletableFuture.completedFuture(varType.deserializeSync(block.valueBytes));

                            return valueFuture.thenAccept(value -> data.put(key, new Object[]{value, varType}));
                        });
                    } else if ("Map".equals(dataType)) {
                        // Même logique pour Map : vérif needAsync pour tous les sous-types
                        boolean mapAsync = VarTypes.STRING.needAsync();
                        boolean keyTypeAsync = VarTypes.STRING.needAsync();
                        boolean valueTypeAsync = VarTypes.STRING.needAsync();

                        CompletableFuture<String> mapTypeFuture = mapAsync
                                ? VarTypes.STRING.deserializeAsync(block.mapTypeBytes)
                                : CompletableFuture.completedFuture(VarTypes.STRING.deserializeSync(block.mapTypeBytes));

                        CompletableFuture<String> keyTypeFuture = keyTypeAsync
                                ? VarTypes.STRING.deserializeAsync(block.keyTypeBytes)
                                : CompletableFuture.completedFuture(VarTypes.STRING.deserializeSync(block.keyTypeBytes));

                        CompletableFuture<String> valueTypeFuture = valueTypeAsync
                                ? VarTypes.STRING.deserializeAsync(block.valueTypeBytes)
                                : CompletableFuture.completedFuture(VarTypes.STRING.deserializeSync(block.valueTypeBytes));

                        return CompletableFuture.allOf(mapTypeFuture, keyTypeFuture, valueTypeFuture).thenCompose(v -> {
                            String mapTypeStr = mapTypeFuture.join();
                            String keyTypeStr = keyTypeFuture.join();
                            String valueTypeStr = valueTypeFuture.join();

                            MapType<?> mapType = MapType.getTypes().get(mapTypeStr);
                            if (mapType == null) throw new CompletionException(new IOException("MapType inconnu: " + mapTypeStr));

                            VarSubType<Object> varType1 = (VarSubType<Object>) VarType.getTypes().get(keyTypeStr);
                            VarSubType<Object> varType2 = (VarSubType<Object>) VarType.getTypes().get(valueTypeStr);
                            if (varType1 == null || varType2 == null)
                                throw new CompletionException(new IOException("VarType inconnu pour map"));

                            MapVarType<Object, Object> mapVarType = new MapVarType<>(mapType, varType1, varType2);

                            CompletableFuture<Map<Object, Object>> mapFuture = mapVarType.needAsync()
                                    ? mapVarType.deserializeAsync(block.mapBytes)
                                    : CompletableFuture.completedFuture(mapVarType.deserializeSync(block.mapBytes));

                            return mapFuture.thenAccept(map -> data.put(key, new Object[]{map, mapVarType}));
                        });
                    } else {
                        throw new CompletionException(new IOException("Type de données inconnu: " + dataType));
                    }
                }).thenCompose(Function.identity());

                futures.add(blockFuture);
            }

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        });
    }


    private static class BlockData {
        byte[] dataTypeBytes, keyBytes;
        byte[] typeBytes, valueBytes; // Wrapper
        byte[] mapTypeBytes, keyTypeBytes, valueTypeBytes, mapBytes; // Map

        public BlockData(byte[] dataTypeBytes, byte[] keyBytes, byte[] typeBytes, byte[] valueBytes,
                         byte[] mapTypeBytes, byte[] keyTypeBytes, byte[] valueTypeBytes, byte[] mapBytes) {
            this.dataTypeBytes = dataTypeBytes;
            this.keyBytes = keyBytes;
            this.typeBytes = typeBytes;
            this.valueBytes = valueBytes;
            this.mapTypeBytes = mapTypeBytes;
            this.keyTypeBytes = keyTypeBytes;
            this.valueTypeBytes = valueTypeBytes;
            this.mapBytes = mapBytes;
        }
    }




    private static byte[]readByteArray(@NotNull ByteBuffer buffer) {
        final byte[]bytes=new byte[buffer.getInt()];
        buffer.get(bytes);
        return bytes;
    }
    private void putByteArray(@NotNull ByteBuffer buffer,byte[]bytes){
        buffer.putInt(bytes.length);
        buffer.put(bytes);
    }
}