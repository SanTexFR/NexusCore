package fr.nexus.listeners.events;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public abstract class CoreEvent<E>{
    //VARIABLES (INSTANCES)
    private boolean asyncCancelled;
    private final@NotNull E bukkitEvent;

    //CONSTRUCTOR
    public CoreEvent(@NotNull E bukkitEvent) {
        this.bukkitEvent = bukkitEvent;
    }


    //METHODS (INSTANCES)

    //EVENT
    public@NotNull E getBukkitEvent() {
        return bukkitEvent;
    }
    public abstract@NotNull Object getKey();

    //ASYNC
    public boolean isAsyncCancelled() {
        return asyncCancelled;
    }
    public void setAsyncCancelled(boolean asyncCancelled) {
        this.asyncCancelled = asyncCancelled;
    }

    //RESULT
    public abstract boolean applyResult();
}
