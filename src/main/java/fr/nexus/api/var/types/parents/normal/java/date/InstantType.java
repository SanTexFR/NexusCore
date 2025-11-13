package fr.nexus.api.var.types.parents.normal.java.date;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class InstantType extends VarType<Instant>{
    //CONSTRUCTOR
    public InstantType(){
        super(Instant.class,1);
    }


    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Instant value){
        return addVersionToBytes(VarTypes.LONG.serializeSync(value.toEpochMilli()));
    }
    public@NotNull Instant deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull Instant deserialize(int version,byte[]bytes){
        if(version==1)return Instant.ofEpochMilli(VarTypes.LONG.deserializeSync(bytes));
        else throw createUnsupportedVersionException(version);
    }
}