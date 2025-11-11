package fr.nexus.listeners;

import fr.nexus.listeners.events.CoreBlockBreakEvent;
import fr.nexus.listeners.events.CoreBlockPlaceEvent;
import org.bukkit.Material;

public class Test{
    static{
        Listeners.registerAsync(
                CoreBlockBreakEvent.class,
                null,
                (coreEvent, value) -> {
                    try { Thread.sleep(2); } catch (InterruptedException ignored) {}
                    return getBlockValue(coreEvent.getBukkitEvent().getBlock().getType());
                },
                null,
                ((coreEvent, value) ->{
                    coreEvent.getBukkitEvent().getPlayer().sendMessage(
                            "Bien déroulé: "+value
                    );
                })
        );

        Listeners.registerAsync(
                CoreBlockPlaceEvent.class,
                null,
                (coreEvent, preValue) -> {
                    try { Thread.sleep(2); } catch (InterruptedException ignored) {}
                    return getBlockValue(coreEvent.getBukkitEvent().getBlock().getType());
                },
                null,
                (coreEvent, syncValue) -> {
                    coreEvent.getBukkitEvent().getPlayer().sendMessage("Bien déroulé: "+syncValue);
                }
        );
    }
    private static int getBlockValue(Material material) {
        return switch(material) {
            case DIAMOND_ORE -> 100;
            case GOLD_ORE -> 50;
            case IRON_ORE -> 20;
            default -> 1;
        };
    }
}
