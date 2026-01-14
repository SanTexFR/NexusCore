package fr.nexus.api.var.types.parents.normal.java;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.InternalVarType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class AtomicIntegerType extends InternalVarType<AtomicInteger>{
    //METHODS
    public byte@NotNull[]serializeSync(@NotNull AtomicInteger value){
        return VarTypes.INTEGER.serializeSync(value.get());
    }
    public@NotNull AtomicInteger deserializeSync(int version,byte[]bytes){
        return new AtomicInteger(VarTypes.INTEGER.deserializeSync(version,bytes));
    }
}