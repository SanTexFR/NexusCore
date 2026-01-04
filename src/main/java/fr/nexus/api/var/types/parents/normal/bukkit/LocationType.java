package fr.nexus.api.var.types.parents.normal.bukkit;

import fr.nexus.api.var.types.VarTypes;
import fr.nexus.api.var.types.parents.InternalVarType;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class LocationType extends InternalVarType<Location>{
    //METHODS
    public byte@NotNull[] serializeSync(@NotNull Location value){
        final byte[]serializedWorld=VarTypes.WORLD.serializeSync(value.getWorld());
        final ByteBuffer buffer=ByteBuffer.allocate(Integer.BYTES+Double.BYTES*3+Float.BYTES*2+serializedWorld.length);

        //WORLD
        buffer.putInt(serializedWorld.length);
        buffer.put(serializedWorld);

        //X,Y,Z
        buffer.putDouble(value.getX());
        buffer.putDouble(value.getY());
        buffer.putDouble(value.getZ());

        //YAW,PITCH
        buffer.putFloat(value.getYaw());
        buffer.putFloat(value.getPitch());

        return addVersionToBytes(buffer.array());
    }
    public@NotNull Location deserializeSync(int version, byte[]bytes){
        if(version==1){
            final ByteBuffer buffer=ByteBuffer.wrap(bytes);
            final byte[]worldBytes=new byte[buffer.getInt()];
            buffer.get(worldBytes);
            final World world=VarTypes.WORLD.deserializeSync(worldBytes);
            final double x=buffer.getDouble(),
                    y=buffer.getDouble(),
                    z=buffer.getDouble();
            final float yaw=buffer.getFloat(),
                    pitch=buffer.getFloat();
            return new Location(world,x,y,z,yaw,pitch);
        }else throw createUnsupportedVersionException(version);
    }
}