package fr.nexus.api.var.types.parents.map;

import fr.nexus.api.var.types.VarSubType;
import fr.nexus.api.var.types.VarVersion;
import fr.nexus.api.var.types.parents.Vars;
import fr.nexus.utils.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"unused","UnusedReturnValue","unchecked"})
public class MapVarType<T,T2>extends VarVersion implements Vars,CollectionUtils{
    //VARIABLES (STATICS)
    private static final int VERSION=1;

    //VARIABLES (INSTANCES)
    private final@NotNull MapType<?>varMapType;
    private final@NotNull VarSubType<T>keyVarType;
    private final@NotNull VarSubType<T2>valueVarType;

    //CONSTRUCTOR
    public MapVarType(@NotNull MapType<?> varMapType, @NotNull VarSubType<T> keyVarType, @NotNull VarSubType<T2>valueVarType){
        super(VERSION);
        this.varMapType=varMapType;
        this.keyVarType=keyVarType;
        this.valueVarType=valueVarType;
    }


    //METHODS

    //TYPE
    public@NotNull MapType<?>getVarMapType(){
        return this.varMapType;
    }
    public@NotNull VarSubType<T>getKeyVarType(){
        return this.keyVarType;
    }
    public@NotNull VarSubType<T2>getValueVarType(){
        return this.valueVarType;
    }

    public boolean isMap(){
        return true;
    }

    //RAW TYPE
    public @NotNull String getStringType(){
        return"Map<"+this.varMapType.getStringType()+"¦"+this.keyVarType.getStringType()+"¦"+this.valueVarType.getStringType()+">";
    }

    //SERIALIZATION

    //SYNC
    public byte[]serializeSync(@NotNull Map<T,T2>map){
        byte[][]keys=new byte[map.size()][],
                values=new byte[map.size()][];

        int index=0,length=0;
        for(final Map.Entry<T,T2>entry:map.entrySet()){
            final byte[] key=this.keyVarType.serializeSync(entry.getKey()),
                    value=this.valueVarType.serializeSync(entry.getValue());

            length+=Integer.BYTES*2+key.length+value.length;
            keys[index]=key;
            values[index]=value;
            index++;
        }

        final ByteBuffer buffer=ByteBuffer.allocate(Integer.BYTES+length);
        buffer.putInt(map.size());
        for(int i=0;i<map.size();i++){
            buffer.putInt(keys[i].length);
            buffer.put(keys[i]);

            buffer.putInt(values[i].length);
            buffer.put(values[i]);
        }

        return addVersionToBytes(buffer.array());
    }
    public @NotNull Map<T,T2>deserializeSync(byte[]bytes){
        final int version=readVersionAndRemainder(bytes);
        return deserializeSync(version,bytes);
    }


    public@NotNull Map<T,T2>deserializeSync(int version, byte[]bytes){
        if(version==1){
            final ByteBuffer buffer=ByteBuffer.wrap(bytes);
            final Map<T,T2>map=(Map<T,T2>)this.varMapType.getSupplier().get();

            final int mapSize=buffer.getInt();
            for(int i=0;i<mapSize;i++){
                //KEY
                final byte[]keyBytes=new byte[buffer.getInt()];
                buffer.get(keyBytes);

                //VALUE
                final byte[]valueBytes=new byte[buffer.getInt()];
                buffer.get(valueBytes);

                //ENTRY
                map.put(this.keyVarType.deserializeSync(keyBytes),this.valueVarType.deserializeSync(valueBytes));
            }return map;
        }else throw createUnsupportedVersionException(version);
    }

    //ASYNC
    public @NotNull CompletableFuture<byte @NotNull []> serializeAsync(@NotNull Map<T, T2> map) {
        // Tableau pour stocker les futures de clés et valeurs sérialisées
        final byte[][] keys = new byte[map.size()][];
        final byte[][] values = new byte[map.size()][];

        // Liste des futures de sérialisation
        List<CompletableFuture<Void>> futures = new ArrayList<>(map.size());

        final Iterator<Map.Entry<T, T2>> iterator = map.entrySet().iterator();

        for (int i = 0; i < map.size(); i++) {
            final int index = i;
            Map.Entry<T, T2> entry = iterator.next();

            CompletableFuture<byte[]> keyFuture = this.keyVarType.serializeAsync(entry.getKey());
            CompletableFuture<byte[]> valueFuture = this.valueVarType.serializeAsync(entry.getValue());

            // Combine les deux futures (clé + valeur), puis stocke dans les tableaux
            CompletableFuture<Void> combinedFuture = keyFuture.thenCombine(valueFuture, (keyBytes, valueBytes) -> {
                keys[index] = keyBytes;
                values[index] = valueBytes;
                return null;
            });

            futures.add(combinedFuture);
        }

        // Quand toutes les clés et valeurs sont sérialisées, on construit le ByteBuffer final.
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    int length = 0;
                    for (int i = 0; i < map.size(); i++) {
                        length += Integer.BYTES * 2 + keys[i].length + values[i].length;
                    }

                    ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + length);
                    buffer.putInt(map.size());
                    for (int i = 0; i < map.size(); i++) {
                        buffer.putInt(keys[i].length);
                        buffer.put(keys[i]);
                        buffer.putInt(values[i].length);
                        buffer.put(values[i]);
                    }

                    return addVersionToBytes(buffer.array());
                });
    }
    public @NotNull CompletableFuture<@NotNull Map<T, T2>> deserializeAsync(byte @NotNull [] bytes) {
        final int version=readVersionAndRemainder(bytes);
        return deserializeAsync(version,bytes);
    }


    public @NotNull CompletableFuture<@NotNull Map<T, T2>> deserializeAsync(int version, byte @NotNull [] bytes) {
        if (version != 1) {
            CompletableFuture<Map<T, T2>> failed = new CompletableFuture<>();
            failed.completeExceptionally(createUnsupportedVersionException(version));
            return failed;
        }

        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        final Map<T, T2> map = (Map<T, T2>) this.varMapType.getSupplier().get();

        final int mapSize = buffer.getInt();
        List<CompletableFuture<Void>> futures = new ArrayList<>(mapSize);

        for (int i = 0; i < mapSize; i++) {
            final byte[] keyBytes = new byte[buffer.getInt()];
            buffer.get(keyBytes);

            final byte[] valueBytes = new byte[buffer.getInt()];
            buffer.get(valueBytes);

            CompletableFuture<Void> future = this.keyVarType.deserializeAsync(keyBytes)
                    .thenCombine(this.valueVarType.deserializeAsync(valueBytes), (key, value) -> {
                        map.put(key,value);
                        return null;
                    });

            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> map);
    }


    //VERSION
    public int getVersion(){
        return VERSION;
    }
}