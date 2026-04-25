package fr.nexus.api.actionBar;

import fr.nexus.api.listeners.Listeners;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ActionBarAPI {
    // ConcurrentHashMap est plus safe si tu as de l'asynchrone
    private static final Map<UUID, PlayerActionBar> activeBars = new ConcurrentHashMap<>();

    static {
        Listeners.register(PlayerQuitEvent.class, e -> remove(e.getPlayer()));
    }

    @NotNull
    public static PlayerActionBar get(@NotNull Player player) {
        return activeBars.computeIfAbsent(player.getUniqueId(), k -> new PlayerActionBar(player));
    }

    public static void remove(@NotNull Player player) {
        PlayerActionBar bar = activeBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.unload();
        }
    }

    public static void removeSpecific(@NotNull Player p, int priority) {
        PlayerActionBar bar = activeBars.get(p.getUniqueId());
        if (bar != null) {
            bar.removePriority(priority);
        }
    }

    public static void removeSpecific(@NotNull Player p, @NotNull ActionBarPriority priority) {
        removeSpecific(p, priority.level());
    }

    public static boolean exists(@NotNull Player p, int priority) {
        PlayerActionBar bar = activeBars.get(p.getUniqueId());
        return bar != null && bar.hasPriority(priority);
    }

    public static boolean exists(@NotNull Player p, @NotNull ActionBarPriority priority) {
        return exists(p, priority.level());
    }

    // --- MÉTHODES DE COMMODITÉ (POUR ALLER VITE) ---

    public static void sendText(@NotNull Player p, @NotNull Component text, int durationTicks) {
        get(p).addEntry(ActionBarEntry.builder()
                .text(text)
                .duration(durationTicks)
                .build());
    }

    public static void clear(@NotNull Player p) {
        PlayerActionBar bar = activeBars.get(p.getUniqueId());
        if (bar != null) bar.clearAll();
    }
}