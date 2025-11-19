package fr.nexus.api.var.types.parents.normal.bukkit;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.normal.VarType;
import fr.nexus.api.var.types.parents.normal.java.IntegerType;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class DeprecatedMaterialType extends VarType<Material>{
    //CONSTRUCTOR
    public DeprecatedMaterialType(){
        super(Material.class,1);
    }


    //METHODS
    public byte@NotNull[]serializeSync(@NotNull Material value){
        return addVersionToBytes(IntegerType.toVarInt(value.ordinal()));
    }
    public@NotNull Material deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull Material deserialize(int version,byte[]bytes){
        if(version!=1)throw createUnsupportedVersionException(version);

        final int ordinal=IntegerType.fromVarInt(bytes);
        return Material.values()[ordinal];
    }
}