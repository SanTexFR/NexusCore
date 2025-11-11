package fr.nexus.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

@SuppressWarnings({"unused","UnusedReturnValue"})
public interface CollectionUtils{
    //SYNC
    default<T>byte@NotNull[]serializeCollection(@NotNull Collection<@NotNull T>collection,@NotNull Function<@NotNull T,byte@NotNull[]>serializationMethod){
        final byte[][]bytes=new byte[collection.size()][];

        int length=0,count=0;
        for(final T obj:collection){
            bytes[count]=serializationMethod.apply(obj);
            length+=bytes[count].length+Integer.BYTES;
            count++;
        }
        final ByteBuffer buffer=ByteBuffer.allocate(length);
        for(byte[]data:bytes){
            buffer.putInt(data.length);
            buffer.put(data);
        }

        return buffer.array();
    }
    default@Nullable<T>Collection<@Nullable T>deserializeCollection(byte@NotNull[]bytes,@NotNull Collection<T>collection,@NotNull Function<byte@NotNull[],?extends@Nullable T>deserializationMethod){
        final ByteBuffer buffer=ByteBuffer.wrap(bytes);
        while(buffer.hasRemaining()){
            final byte[]objData=new byte[buffer.getInt()];
            buffer.get(objData);
            collection.add(deserializationMethod.apply(objData));
        }return collection;
    }

    //ASYNC
    default<T>@NotNull CompletableFuture<byte@NotNull []>serializeCollectionAsync(@NotNull Collection<@NotNull T>collection,@NotNull Function<@NotNull T,@NotNull CompletableFuture<byte@NotNull []>>serializationMethod){
        final List<CompletableFuture<byte[]>>futures=collection.stream()
                .map(serializationMethod)
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    try {
                        byte[][] byteArrays = new byte[collection.size()][];
                        int length = 0;
                        for (int i = 0; i < futures.size(); i++) {
                            byteArrays[i] = futures.get(i).join(); // safe here, all completed
                            length += Integer.BYTES + byteArrays[i].length;
                        }

                        ByteBuffer buffer = ByteBuffer.allocate(length);
                        for (byte[] data : byteArrays) {
                            buffer.putInt(data.length);
                            buffer.put(data);
                        }

                        return buffer.array();
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                });
    }
    default<T>@NotNull CompletableFuture<Collection<T>>deserializeCollectionAsync(byte @NotNull[]bytes,@NotNull Collection<T>collection,@NotNull Function<byte[],@NotNull CompletableFuture<T>>deserializationMethod){
        final ByteBuffer buffer=ByteBuffer.wrap(bytes);
        final List<CompletableFuture<T>>futures=new ArrayList<>();

        while (buffer.hasRemaining()) {
            byte[] objData = new byte[buffer.getInt()];
            buffer.get(objData);
            futures.add(deserializationMethod.apply(objData));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    for (CompletableFuture<T> future : futures) {
                        collection.add(future.join()); // safe, all completed
                    }
                    return collection;
                });
    }

}