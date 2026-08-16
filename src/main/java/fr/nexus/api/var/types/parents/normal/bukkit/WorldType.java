package fr.nexus.api.var.types.parents.normal.bukkit;

import fr.nexus.Core;
import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.InternalVarType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class WorldType extends InternalVarType<World> {
    //METHODS
    public byte@NotNull[] serializeSync(@NotNull World value){
        return addVersionToBytes(VarTypes.STRING.serializeSync(value.getName()));
    }
    public@NotNull World deserializeSync(int version,byte[]bytes){
        if(version==1){
            World world=Bukkit.getWorld(VarTypes.STRING.deserializeSync(bytes));
            if(world==null){
                world=Bukkit.getWorld("worlds_"+VarTypes.STRING.deserializeSync(bytes));
                if(world==null){
                    if(Core.enableFallBackWorld){
                        world=Bukkit.getWorlds().getFirst();
                        if(world==null)throw new RuntimeException("World doesn't exist: "+VarTypes.STRING.deserializeSync(bytes));
                    }else throw new RuntimeException("World doesn't exist: "+VarTypes.STRING.deserializeSync(bytes));
                }
            }
            return world;
        } else throw createUnsupportedVersionException(version);
    }
}