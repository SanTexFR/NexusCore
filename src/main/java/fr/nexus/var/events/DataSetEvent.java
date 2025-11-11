package fr.nexus.var.events;

import fr.nexus.utils.EventMesh;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class DataSetEvent extends EventMesh{
    //VARIABLES (INSTANCES)
    private final@NotNull DataSetEventType type;
    private final@NotNull String key;
    private final@Nullable Object value;

    //CONSTRUCTOR
    public DataSetEvent(@NotNull DataSetEventType type,@NotNull String key,@Nullable Object value){
        this.type=type;
        this.key=key;
        this.value=value;
    }


    //METHODS (INSTANCES)
    public@NotNull DataSetEventType getType(){
        return this.type;
    }
    public@NotNull String getKey(){
        return this.key;
    }
    public@Nullable Object getValue(){
        return this.value;
    }
}