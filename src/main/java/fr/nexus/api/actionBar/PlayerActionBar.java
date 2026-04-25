package fr.nexus.api.actionBar;

import com.cjcrafter.foliascheduler.TaskImplementation;
import fr.nexus.Core;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.TreeMap;

public class PlayerActionBar {
    private final Player player;

    // Plus de listes ! Une priorité = UN SEUL message en attente.
    // Si un nouveau arrive avec la même priorité, il écrase l'ancien grâce au .put()
    private final TreeMap<Integer, ActionBarEntry> waitingEntries = new TreeMap<>();

    private @Nullable ActionBarEntry currentEntry = null;
    private @Nullable TaskImplementation<?> tickTask = null;
    private int tickCounter = 0;

    public PlayerActionBar(@NotNull Player player) {
        this.player = player;
    }

    public void addEntry(@NotNull ActionBarEntry newEntry) {
        int newPriority = newEntry.getPriority();

        if (currentEntry != null) {
            if (newPriority < currentEntry.getPriority()) {
                // Le nouveau est PLUS prioritaire.
                // On met le message actuel en pause et on le sauvegarde dans les attentes.
                waitingEntries.put(currentEntry.getPriority(), currentEntry);
                switchTo(newEntry);
            } else if (newPriority == currentEntry.getPriority()) {
                // MÊME priorité.
                // On écrase purement et simplement le message en cours de lecture.
                switchTo(newEntry);
            } else {
                // MOINS prioritaire. Il va dans la salle d'attente.
                // S'il y avait déjà un message de sa priorité en attente, il l'écrase automatiquement.
                waitingEntries.put(newPriority, newEntry);
            }
        } else {
            // Rien n'est en cours de lecture, on affiche direct.
            switchTo(newEntry);
        }

        startTickingIfNeeded();
    }

    // Petite méthode pour éviter de se répéter
    private void switchTo(@NotNull ActionBarEntry entry) {
        this.currentEntry = entry;
        this.tickCounter = 0; // Force l'affichage immédiat au prochain tick
    }

    private void startTickingIfNeeded() {
        if (tickTask == null) {
            tickTask = Core.getServerImplementation().entity(player).runAtFixedRate(this::tick, 1L, 1L);
        }
    }

    private void tick() {
        if (!player.isOnline()) {
            unload();
            return;
        }

        // Si on a fini de lire le message actuel, on prend le plus prioritaire en attente
        if (currentEntry == null) {
            if (waitingEntries.isEmpty()) {
                unload(); // Plus rien du tout, on éteint la tâche
                return;
            }

            // On récupère et supprime la plus petite clé (la plus haute priorité)
            int highestPriority = waitingEntries.firstKey();
            currentEntry = waitingEntries.remove(highestPriority);
            tickCounter = 0;
        }

        // Fin du message
        if (currentEntry.getRemainingTicks() <= 0) {
            currentEntry = null;
            player.sendActionBar(Component.empty());
            return; // On laisse le tick suivant prendre le prochain message
        }

        // Affichage selon le updateRate
        if (tickCounter % currentEntry.getUpdateRateTicks() == 0) {
            try {
                player.sendActionBar(currentEntry.getTextSupplier().get());
            } catch (Exception e) {
                player.sendActionBar(Component.empty());
            }
        }

        currentEntry.decrementTicks();
        tickCounter++;
    }

    public void skipCurrent() {
        this.currentEntry = null;
        this.player.sendActionBar(Component.empty());
    }

    public void removePriority(int priority) {
        // 1. Si la priorité demandée est celle actuellement affichée
        if (currentEntry != null && currentEntry.getPriority() == priority) {
            this.currentEntry = null;
            this.player.sendActionBar(Component.empty());
            // Le tick() suivant s'occupera de prendre le prochain message dans waitingEntries
        }

        // 2. On la supprime aussi de la salle d'attente (si elle y était)
        this.waitingEntries.remove(priority);

        // 3. Si plus rien n'est à afficher du tout, on coupe la tâche
        if (currentEntry == null && waitingEntries.isEmpty()) {
            unload();
        }
    }

    public boolean hasPriority(int priority) {
        // 1. Est-ce que c'est le message actuellement à l'écran ?
        if (currentEntry != null && currentEntry.getPriority() == priority) {
            return true;
        }

        // 2. Est-ce qu'elle est dans la salle d'attente ?
        return waitingEntries.containsKey(priority);
    }

    // Version avec ton Record
    public boolean hasPriority(@NotNull ActionBarPriority priority) {
        return hasPriority(priority.level());
    }

    // Surcharge pour utiliser ton Record ActionBarPriority
    public void removePriority(@NotNull ActionBarPriority priority) {
        removePriority(priority.level());
    }

    public void clearAll() {
        this.waitingEntries.clear();
        this.currentEntry = null;
        this.player.sendActionBar(Component.empty());
        unload();
    }

    public void unload() {
        if (this.tickTask != null) {
            this.tickTask.cancel();
            this.tickTask = null;
        }
    }
}