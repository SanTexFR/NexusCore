package fr.nexus.api.var.types.parents.normal.bukkit;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.VarType;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class GameModeType extends VarType<GameMode>{
    //METHODS
    public byte@NotNull[]serializeSync(@NotNull GameMode value){
        return addVersionToBytes(VarTypes.STRING.serializeSync(value.name()));
    }
    public@NotNull GameMode deserializeSync(int version,byte[]bytes){
        if(version==1){
            return GameMode.valueOf(VarTypes.STRING.deserializeSync(bytes));
        }throw createUnsupportedVersionException(version);
    }
}