package fr.nexus.api.var.types.parents.normal.bukkit;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.normal.VarType;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class MaterialType extends VarType<Material>{
    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Material value){
        return addVersionToBytes(VarTypes.STRING.serializeSync(value.name()));
    }
    public@NotNull Material deserializeSync(int version, byte[]bytes){
        if(version==1)return Material.valueOf(VarTypes.STRING.deserializeSync(bytes));
        else throw createUnsupportedVersionException(version);
    }
}