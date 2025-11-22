package fr.nexus.api.var.types.parents.normal.bukkit.inventory;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.normal.VarType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Arrays;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class InventoryType extends VarType<Inventory>{
    //VARIABLES (STATICS)
    private static final InventoryAccessor ACCESSOR = detectAccessor();
    private static InventoryAccessor detectAccessor() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.Scheduler");
            return new FoliaInventoryAccessor();
        } catch (ClassNotFoundException e) {
            return new PaperInventoryAccessor();
        }
    }

    //CONSTRUCTOR
    public InventoryType(){
        super(Inventory.class,1);
    }


    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Inventory value) {
        return addVersionToBytes(ACCESSOR.serializeSync(value));
    }

    public @NotNull Inventory deserializeSync(byte@NotNull[] bytes) {
        final VersionAndRemainder var = readVersionAndRemainder(bytes);
        return deserialize(var.version(), var.remainder());
    }

    private @NotNull Inventory deserialize(int version, byte[] bytes) {
        if (version == 1) return ACCESSOR.deserializeSync(bytes);
        else throw createUnsupportedVersionException(version);
    }
}