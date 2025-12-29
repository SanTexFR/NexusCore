package fr.nexus.api.var.types.parents.normal.java.date;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class DateType extends VarType<Date>{
    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Date value){
        return addVersionToBytes(VarTypes.LONG.serializeSync(value.getTime()));
    }
    public@NotNull Date deserializeSync(int version,byte[]bytes){
        if(version==1)return new Date(VarTypes.LONG.deserializeSync(bytes));
        else throw createUnsupportedVersionException(version);
    }
}