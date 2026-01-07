package fr.nexus.api.gui;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@SuppressWarnings({"unused","UnusedReturnValue"})
public record GuiReuse(@NotNull String key,@NotNull Supplier<@NotNull CompletableFuture<@NotNull Boolean>> supplier){}