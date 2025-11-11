package fr.nexus.var.types.parents.normal.java.date;

import fr.nexus.var.types.VarTypes;
import fr.nexus.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

import java.time.Period;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class PeriodType extends VarType<Period>{
    //CONSTRUCTOR
    public PeriodType(){
        super(Period.class,1);
    }


    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Period value){
        return VarTypes.INTEGER_ARRAY.serializeSync(new Integer[]{value.getYears(),value.getMonths(),value.getDays()});
    }
    public@NotNull Period deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull Period deserialize(int version,byte[]bytes){
        if(version==1){
            final Integer[]array=VarTypes.INTEGER_ARRAY.deserializeSync(bytes);
            if(array!=null)return Period.of(array[0],array[1],array[2]);
        }throw createUnsupportedVersionException(version);
    }
}