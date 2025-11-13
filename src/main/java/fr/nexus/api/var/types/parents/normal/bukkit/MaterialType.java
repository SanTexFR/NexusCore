package fr.nexus.api.var.types.parents.normal.bukkit;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.normal.VarType;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class MaterialType extends VarType<Material>{
    //CONSTRUCTOR
    public MaterialType(){
        super(Material.class,1);
    }


    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Material value){
        return addVersionToBytes(VarTypes.STRING.serializeSync(value.name()));
    }
    public@NotNull Material deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull Material deserialize(int version, byte[]bytes){
        if(version==1)return Material.valueOf(VarTypes.STRING.deserializeSync(bytes));
        else throw createUnsupportedVersionException(version);
    }
}