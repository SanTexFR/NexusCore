package fr.nexus.api.var.types.parents.normal.bukkit;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.normal.VarType;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class GameModeType extends VarType<GameMode>{
    //CONSTRUCTOR
    public GameModeType(){
        super(GameMode.class,1);
    }


    //METHODS
    public byte@NotNull[]serializeSync(@NotNull GameMode value){
        return addVersionToBytes(VarTypes.STRING.serializeSync(value.name()));
    }
    public@NotNull GameMode deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull GameMode deserialize(int version,byte[]bytes){
        if(version==1){
            return GameMode.valueOf(VarTypes.STRING.deserializeSync(bytes));
        }throw createUnsupportedVersionException(version);
    }
}