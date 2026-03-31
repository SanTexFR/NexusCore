package fr.nexus.api.cooldown;

import com.cjcrafter.foliascheduler.TaskImplementation;
import fr.nexus.Core;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class Cooldown{
    private static final@NotNull ConcurrentHashMap<@NotNull String,@NotNull CooldownData>cooldowns=new ConcurrentHashMap<>();

    public static boolean isOnCooldown(@NotNull String id){
        return cooldowns.containsKey(id);
    }
    public static@Nullable Long getCooldown(@NotNull String id){
        final CooldownData cooldown=cooldowns.get(id);
        return cooldown != null ? cooldown.millis : null;
    }
    public static void createCooldown(@NotNull String id,long ticks){
        CooldownData existing = cooldowns.get(id);
        if (existing != null) {
            existing.task.cancel();
        }

        long endMillis = System.currentTimeMillis() + ticks * 50;

        TaskImplementation<?> task = Core.getServerImplementation().global().runDelayed(
                () -> cooldowns.remove(id),
                ticks
        );

        cooldowns.put(id, new CooldownData(endMillis, task));
    }

    public static void cancelCooldown(@NotNull String id) {
        CooldownData data = cooldowns.remove(id);
        if (data != null) {
            data.task.cancel(); // Stoppe la tâche de suppression planifiée
        }
    }

    private record CooldownData(long millis,@NotNull TaskImplementation<?> task){}
}