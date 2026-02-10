package fr.nexus.utils;

import fr.nexus.api.var.types.parents.normal.java.IntegerType; // IMPORTANT
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

@SuppressWarnings({"unused","UnusedReturnValue"})
public interface CollectionUtils {

    // SYNC (STREAMING + VARINT)
    default <T> byte @NotNull [] serializeCollection(@NotNull Collection<@NotNull T> collection, @NotNull Function<@NotNull T, byte @NotNull []> serializationMethod) {
        try {
            // Taille estimée (50 octets par élément)
            ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.max(128, collection.size() * 50));

            // On écrit la taille de la collection d'abord (VarInt)
            // Comme ça à la lecture on sait combien d'items lire
            IntegerType.writeVarInt(baos, collection.size());

            for (final T obj : collection) {
                byte[] data = serializationMethod.apply(obj);

                // [Taille VarInt] [Données]
                IntegerType.writeVarInt(baos, data.length);
                baos.write(data);
            }

            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default @Nullable <T> Collection<@Nullable T> deserializeCollection(byte @NotNull [] bytes, @NotNull Collection<T> collection, @NotNull Function<byte @NotNull [], ? extends @Nullable T> deserializationMethod) {
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);

        // Lecture taille collection
        int size = IntegerType.fromVarInt(buffer);

        // On pré-alloue si possible (ArrayList)
        if (collection instanceof ArrayList) {
            ((ArrayList<T>) collection).ensureCapacity(size);
        }

        for (int i = 0; i < size; i++) {
            // Lecture taille item
            int len = IntegerType.fromVarInt(buffer);
            byte[] objData = new byte[len];
            buffer.get(objData);

            collection.add(deserializationMethod.apply(objData));
        }
        return collection;
    }

    // ASYNC (OPTIMISÉ)
    default <T> @NotNull CompletableFuture<byte @NotNull []> serializeCollectionAsync(@NotNull Collection<@NotNull T> collection, @NotNull Function<@NotNull T, @NotNull CompletableFuture<byte @NotNull []>> serializationMethod) {
        // On lance tout en parallèle
        final List<CompletableFuture<byte[]>> futures = new ArrayList<>(collection.size());
        for (T obj : collection) {
            futures.add(serializationMethod.apply(obj));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    try {
                        // Taille estimée
                        ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.max(128, collection.size() * 50));

                        // Taille Collection
                        IntegerType.writeVarInt(baos, collection.size());

                        // Assemblage
                        for (CompletableFuture<byte[]> future : futures) {
                            byte[] data = future.join();
                            IntegerType.writeVarInt(baos, data.length);
                            baos.write(data);
                        }

                        return baos.toByteArray();
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }
                });
    }

    default <T> @NotNull CompletableFuture<Collection<T>> deserializeCollectionAsync(byte @NotNull [] bytes, @NotNull Collection<T> collection, @NotNull Function<byte[], @NotNull CompletableFuture<T>> deserializationMethod) {
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);

        // Taille Collection
        int size = IntegerType.fromVarInt(buffer);
        final List<CompletableFuture<T>> futures = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            int len = IntegerType.fromVarInt(buffer);
            byte[] objData = new byte[len];
            buffer.get(objData);

            futures.add(deserializationMethod.apply(objData));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    for (CompletableFuture<T> future : futures) {
                        collection.add(future.join());
                    }
                    return collection;
                });
    }
}