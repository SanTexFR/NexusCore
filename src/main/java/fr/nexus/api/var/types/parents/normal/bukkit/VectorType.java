package fr.nexus.api.var.types.parents.normal.bukkit;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.normal.VarType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class VectorType extends VarType<Vector>{
    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Vector value){
        return VarTypes.DOUBLE_ARRAY.serializeSync(new Double[]{value.getX(),value.getY(),value.getZ()});
    }
    public@NotNull Vector deserializeSync(int version,byte[]bytes){
        if(version==1){
            final Double[]array=VarTypes.DOUBLE_ARRAY.deserializeSync(bytes);
            if(array!=null)return new Vector(array[0],array[1],array[2]);
        }throw createUnsupportedVersionException(version);
    }
}