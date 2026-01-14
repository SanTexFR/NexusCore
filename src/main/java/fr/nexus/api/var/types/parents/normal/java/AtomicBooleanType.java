package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.InternalVarType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class AtomicBooleanType extends InternalVarType<AtomicBoolean>{
    //METHODS
    public byte@NotNull[]serializeSync(@NotNull AtomicBoolean value){
        return VarTypes.BOOLEAN.serializeSync(value.get());
    }
    public@NotNull AtomicBoolean deserializeSync(int version,byte[]bytes){
        return new AtomicBoolean(VarTypes.BOOLEAN.deserializeSync(version,bytes));
    }
}