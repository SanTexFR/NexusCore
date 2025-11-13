package fr.nexus.api.var.types;

import fr.nexus.api.var.types.parents.Vars;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"unused","UnusedReturnValue"})
public interface VarSubType<V>extends Vars{
    byte@NotNull[] serializeSync(@NotNull V value);
    @Nullable V deserializeSync(byte@NotNull[]bytes);

    @NotNull CompletableFuture<byte@NotNull[]>serializeAsync(@NotNull V value);
    @NotNull CompletableFuture<@Nullable V>deserializeAsync(byte@NotNull[]bytes);
}