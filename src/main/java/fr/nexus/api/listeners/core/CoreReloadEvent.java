package fr.nexus.api.listeners.core;

import fr.nexus.utils.EventMesh;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class CoreReloadEvent extends EventMesh{
    //VARIABLES(INSTANCES)
    private final boolean safe;

    //CONSTRUCTOR
    public CoreReloadEvent(boolean safe){
        this.safe=safe;
    }

    //METHODS(INSTANCES)
    public boolean isSafe(){
        return this.safe;
    }
}