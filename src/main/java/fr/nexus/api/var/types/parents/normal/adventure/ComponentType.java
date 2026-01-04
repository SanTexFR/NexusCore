package fr.nexus.api.var.types.parents.normal.adventure;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.InternalVarType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class ComponentType extends InternalVarType<Component>{
    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Component value){
        return addVersionToBytes(VarTypes.STRING.serializeSync(MiniMessage.miniMessage().serialize(value)));
    }
    public@NotNull Component deserializeSync(int version, byte[]bytes){
        if(version==1)return MiniMessage.miniMessage().deserialize(VarTypes.STRING.deserializeSync(bytes));
        else throw createUnsupportedVersionException(version);
    }
}