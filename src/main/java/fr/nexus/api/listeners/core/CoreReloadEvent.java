package fr.nexus.api.listeners.core;

import fr.nexus.utils.EventMesh;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class CoreReloadEvent extends EventMesh{
    //VARIABLES(INSTANCES)
    private final@Nullable String key;
    private final boolean safe;

    //CONSTRUCTOR
    public CoreReloadEvent(@Nullable String key,boolean safe){
        this.key=key;
        this.safe=safe;
    }

    //METHODS(INSTANCES)
    public@Nullable String getKey(){
        return this.key;
    }
    public boolean isSafe(){
        return this.safe;
    }
}