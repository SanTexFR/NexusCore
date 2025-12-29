package fr.nexus.api.var.types.parents.normal.java.date;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.normal.VarType;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class LocalDateTimeType extends VarType<LocalDateTime>{
    //METHODS
    public byte@NotNull[] serializeSync(@NotNull LocalDateTime value){
        return addVersionToBytes(VarTypes.LONG.serializeSync(value.toInstant(ZoneOffset.UTC).toEpochMilli()));
    }
    public@NotNull LocalDateTime deserializeSync(int version,byte[]bytes){
        if(version==1){
            long millis=VarTypes.LONG.deserializeSync(bytes);
            return LocalDateTime.ofEpochSecond(millis / 1000, (int) (millis % 1000) * 1_000_000, ZoneOffset.UTC);
        }else throw createUnsupportedVersionException(version);
    }
}