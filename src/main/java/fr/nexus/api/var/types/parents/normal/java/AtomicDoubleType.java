package fr.nexus.api.var.types.parents.normal.java;

import com.google.common.util.concurrent.AtomicDouble;
import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.InternalVarType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class AtomicDoubleType extends InternalVarType<AtomicDouble>{
    //METHODS
    public byte@NotNull[]serializeSync(@NotNull AtomicDouble value){
        return VarTypes.DOUBLE.serializeSync(value.get());
    }
    public@NotNull AtomicDouble deserializeSync(int version,byte[]bytes){
        return new AtomicDouble(VarTypes.DOUBLE.deserializeSync(version,bytes));
    }
}