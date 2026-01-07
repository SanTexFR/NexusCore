package fr.nexus.api.listeners.core;

import fr.nexus.Core;
import fr.nexus.utils.EventMesh;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class CoreCleanupEvent extends EventMesh{
    private final@NotNull UUID uuid=UUID.randomUUID();

    public CoreCleanupEvent(){
        Core.setLastCleanupUUID(this.uuid);
    }

    public@NotNull UUID getUUID(){
        return this.uuid;
    }
}