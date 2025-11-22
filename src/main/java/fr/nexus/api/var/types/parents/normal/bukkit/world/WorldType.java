package fr.nexus.api.var.types.parents.normal.bukkit.world;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.normal.VarType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused","UnusedReturnValue"})
//public final class WorldType extends VarType<World>{
//    //CONSTRUCTOR
//    public WorldType(){
//        super(World.class,1);
//    }
//
//
//    //METHODS
//    public byte@NotNull[] serializeSync(@NotNull World value){
//        return addVersionToBytes(VarTypes.STRING.serializeSync(value.getName()));
//    }
//    public@NotNull World deserializeSync(byte@NotNull[]bytes){
//        final VersionAndRemainder var=readVersionAndRemainder(bytes);
//        return deserialize(var.version(),var.remainder());
//    }
//
//    private@NotNull World deserialize(int version,byte[]bytes){
//        if(version==1){
//            final World world=Bukkit.getWorld(VarTypes.STRING.deserializeSync(bytes));
//            if(world==null)throw new RuntimeException("Word doesn't exist: "+VarTypes.STRING.deserializeSync(bytes));
//            return world;
//        } else throw createUnsupportedVersionException(version);
//    }
//}

public final class WorldType extends VarType<World>{
    private static final WorldAccessor ACCESSOR = detectAccessor();

    private static WorldAccessor detectAccessor(){
        try{
            Class.forName("io.papermc.paper.threadedregions.scheduler.Scheduler");
            return new FoliaWorldAccessor();
        } catch (ClassNotFoundException e) {
            return new PaperWorldAccessor();
        }
    }


    public WorldType() {
        super(World.class,1);
    }

    public byte@NotNull[] serializeSync(@NotNull World value){
        return addVersionToBytes(VarTypes.STRING.serializeSync(ACCESSOR.getName(value)));
    }

    public @NotNull World deserializeSync(byte@NotNull[] bytes){
        final VersionAndRemainder var = readVersionAndRemainder(bytes);
        return deserialize(var.version(), var.remainder());
    }

    private @NotNull World deserialize(int version, byte[] bytes){
        if(version==1){
            final World world = ACCESSOR.getWorld(VarTypes.STRING.deserializeSync(bytes));
            if(world==null) throw new RuntimeException("World doesn't exist: " + VarTypes.STRING.deserializeSync(bytes));
            return world;
        } else throw createUnsupportedVersionException(version);
    }
}