package fr.nexus.listeners.events;

import fr.nexus.listeners.Listeners;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class CoreEntityDamageByEntityEvent extends CoreEvent<EntityDamageByEntityEvent>{
    // VARIABLES (STATICS)
    static {
        Listeners.registerCoreBridge(EntityDamageByEntityEvent.class,CoreEntityDamageByEntityEvent::new);
    }

    // VARIABLES (INSTANCES)
    private final@NotNull Entity target;

    // CONSTRUCTOR
    public CoreEntityDamageByEntityEvent(@NotNull EntityDamageByEntityEvent e){
        super(e);

        this.target=e.getEntity();
    }

    // METHODS (OVERRIDES)
    @Override
    public boolean applyResult(){
        if(isAsyncCancelled())return false;

        final EntityDamageByEntityEvent e=getBukkitEvent();

        e.getDamage();
        e.getFinalDamage();

        return true;
    }

    @Override
    public@NotNull Object getKey(){
        return this.target.getUniqueId();
    }
}