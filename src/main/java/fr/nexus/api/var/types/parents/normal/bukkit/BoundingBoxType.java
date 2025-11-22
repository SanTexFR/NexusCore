package fr.nexus.api.var.types.parents.normal.bukkit;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.normal.VarType;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class BoundingBoxType extends VarType<BoundingBox>{
    //CONSTRUCTOR
    public BoundingBoxType(){
        super(BoundingBox.class,1);
    }


    //METHODS
    public byte@NotNull[]serializeSync(@NotNull BoundingBox value){
        return VarTypes.DOUBLEARRAY.serializeSync(new double[]{value.getMinX(),value.getMinY(),value.getMinZ(), value.getMaxX(),value.getMaxY(),value.getMaxZ()});
    }
    public@NotNull BoundingBox deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull BoundingBox deserialize(int version,byte[]bytes){
        if(version==1){
            final double[]array=VarTypes.DOUBLEARRAY.deserializeSync(bytes);
            return new BoundingBox(array[0],array[1],array[2],array[3],array[4],array[5]);
        }throw createUnsupportedVersionException(version);
    }
}