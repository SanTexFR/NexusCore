package fr.nexus.system.internal.information;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@SuppressWarnings({"unused","UnusedReturnValue"})
public record Information(@NotNull Material material, @NotNull Component title, @NotNull Supplier<@NotNull CompletableFuture<@NotNull Component[]>>supplier){}