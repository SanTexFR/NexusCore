package fr.nexus.performanceTracker;


import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class PerformanceTracker{
    //VARIABLES (STATICS)
    private static final@NotNull ConcurrentHashMap<@NotNull Types,@NotNull ConcurrentHashMap<@NotNull String,TrackerData>>times=new ConcurrentHashMap<>();

    //METHODS (STATICS)

    //KEYS
    public static @NotNull Set<@NotNull String> getMethods(@NotNull Types type) {
        final ConcurrentHashMap<String, TrackerData> map = times.get(type);
        if (map != null) return map.keySet();
        return new HashSet<>();
    }


    //INCREMENT
    public static void increment(@NotNull Types type, @NotNull String method, long nanos) {
        final boolean sync = Bukkit.isPrimaryThread();

        final ConcurrentHashMap<String, TrackerData> methodTimes =
                times.computeIfAbsent(type, k -> new ConcurrentHashMap<>());

        methodTimes.merge(method,
                // Valeur initiale
                new TrackerData(
                        sync ? 1 : 0,
                        sync ? 0 : 1,
                        sync ? nanos : 0L,
                        sync ? 0L : nanos
                ),
                // Fusion avec ancienne valeur
                (old, add) -> new TrackerData(
                        old.syncCount() + (sync ? 1 : 0),
                        old.asyncCount() + (sync ? 0 : 1),
                        old.syncTime() + (sync ? nanos : 0L),
                        old.asyncTime() + (sync ? 0L : nanos)
                )
        );
    }

    //GET
    public static @Nullable TrackerData get(@NotNull Types type, @NotNull String method) {
        final ConcurrentHashMap<String, TrackerData> methodTimes = times.get(type);
        if (methodTimes == null) return null;
        return methodTimes.get(method);
    }

    public static int getSyncCount(@NotNull Types type, @NotNull String method) {
        final TrackerData data = get(type, method);
        return data == null ? 0 : data.syncCount();
    }

    public static int getAsyncCount(@NotNull Types type, @NotNull String method) {
        final TrackerData data = get(type, method);
        return data == null ? 0 : data.asyncCount();
    }

    public static long getSyncTime(@NotNull Types type, @NotNull String method) {
        final TrackerData data = get(type, method);
        return data == null ? 0L : data.syncTime();
    }

    public static long getAsyncTime(@NotNull Types type, @NotNull String method) {
        final TrackerData data = get(type, method);
        return data == null ? 0L : data.asyncTime();
    }

    //INNER CLASS
    public enum Types{
        GUI,
        VAR,
        LISTENER,
        ITEM_BUILDER
    }
    public record TrackerData(int syncCount,int asyncCount,long syncTime,long asyncTime){}
}