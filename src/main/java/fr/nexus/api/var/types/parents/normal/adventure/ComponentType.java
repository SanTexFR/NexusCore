package fr.nexus.api.var.types.parents.normal.adventure;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.normal.VarType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class ComponentType extends VarType<Component>{
    //CONSTRUCTOR
    public ComponentType(){
        super(Component.class,1);
    }


    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Component value){
        return addVersionToBytes(VarTypes.STRING.serializeSync(MiniMessage.miniMessage().serialize(value)));
    }
    public@NotNull Component deserializeSync(byte@NotNull[]bytes){
        final VersionAndRemainder var=readVersionAndRemainder(bytes);
        return deserialize(var.version(),var.remainder());
    }

    private@NotNull Component deserialize(int version, byte[]bytes){
        if(version==1)return MiniMessage.miniMessage().deserialize(VarTypes.STRING.deserializeSync(bytes));
        else throw createUnsupportedVersionException(version);
    }
}