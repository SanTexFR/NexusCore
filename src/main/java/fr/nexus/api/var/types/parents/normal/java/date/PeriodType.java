package fr.nexus.api.var.types.parents.normal.java.date;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.InternalVarType;
import org.jetbrains.annotations.NotNull;

import java.time.Period;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class PeriodType extends InternalVarType<Period>{
    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Period value){
        return VarTypes.INTEGER_ARRAY.serializeSync(new Integer[]{value.getYears(),value.getMonths(),value.getDays()});
    }
    public@NotNull Period deserializeSync(int version,byte[]bytes){
        if(version==1){
            final Integer[]array=VarTypes.INTEGER_ARRAY.deserializeSync(bytes);
            if(array!=null)return Period.of(array[0],array[1],array[2]);
        }throw createUnsupportedVersionException(version);
    }
}