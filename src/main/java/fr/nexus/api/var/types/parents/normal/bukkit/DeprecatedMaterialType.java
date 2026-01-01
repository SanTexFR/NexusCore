package fr.nexus.api.var.types.parents.normal.bukkit;

import fr.nexus.api.var.types.parents.VarType;
import fr.nexus.api.var.types.parents.normal.java.IntegerType;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class DeprecatedMaterialType extends VarType<Material>{
    //METHODS
    public byte@NotNull[]serializeSync(@NotNull Material value){
        return addVersionToBytes(IntegerType.toVarInt(value.ordinal()));
    }
    public@NotNull Material deserializeSync(int version,byte[]bytes){
        if(version!=1)throw createUnsupportedVersionException(version);

        final int ordinal=IntegerType.fromVarInt(bytes);
        return Material.values()[ordinal];
    }
}