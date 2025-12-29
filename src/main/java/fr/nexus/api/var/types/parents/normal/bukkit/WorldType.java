package fr.nexus.api.var.types.parents.normal.bukkit;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.normal.VarType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class WorldType extends VarType<World> {
    //METHODS
    public byte@NotNull[] serializeSync(@NotNull World value){
        return addVersionToBytes(VarTypes.STRING.serializeSync(value.getName()));
    }
    public@NotNull World deserializeSync(int version,byte[]bytes){
        if(version==1){
            final World world=Bukkit.getWorld(VarTypes.STRING.deserializeSync(bytes));
            if(world==null)throw new RuntimeException("Word doesn't exist: "+VarTypes.STRING.deserializeSync(bytes));
            return world;
        } else throw createUnsupportedVersionException(version);
    }
}