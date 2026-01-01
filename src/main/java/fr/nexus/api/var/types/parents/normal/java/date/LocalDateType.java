package fr.nexus.api.var.types.parents.normal.java.date;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.VarType;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class LocalDateType extends VarType<LocalDate>{
    //METHODS
    public byte@NotNull[] serializeSync(@NotNull LocalDate value){
        return addVersionToBytes(VarTypes.LONG.serializeSync(value.toEpochDay()));
    }
    public@NotNull LocalDate deserializeSync(int version,byte[]bytes){
        if(version==1)return LocalDate.ofEpochDay(VarTypes.LONG.deserializeSync(bytes));
        else throw createUnsupportedVersionException(version);
    }
}