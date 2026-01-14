package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.InternalVarType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class AtomicLongType extends InternalVarType<AtomicLong>{
    //METHODS
    public byte@NotNull[]serializeSync(@NotNull AtomicLong value){
        return VarTypes.LONG.serializeSync(value.get());
    }
    public@NotNull AtomicLong deserializeSync(int version,byte[]bytes){
        return new AtomicLong(VarTypes.LONG.deserializeSync(version,bytes));
    }
}