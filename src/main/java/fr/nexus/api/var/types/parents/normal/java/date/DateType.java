package fr.nexus.api.var.types.parents.normal.java.date;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class DateType extends VarType<Date>{
    //CONSTRUCTOR
    public DateType(){
        super(Date.class,1);
    }


    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Date value){
        return addVersionToBytes(VarTypes.LONG.serializeSync(value.getTime()));
    }
    public@NotNull Date deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull Date deserialize(int version,byte[]bytes){
        if(version==1)return new Date(VarTypes.LONG.deserializeSync(bytes));
        else throw createUnsupportedVersionException(version);
    }
}