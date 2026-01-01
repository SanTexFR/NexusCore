package fr.nexus.api.var.types.parents.normal.bukkit;

import fr.nexus.api.var.types.parents.VarType;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class BlockDataType extends VarType<BlockData>{
    //METHODS
    public byte@NotNull[]serializeSync(@NotNull BlockData data){
        final byte[]dataBytes=data.getAsString().getBytes(StandardCharsets.UTF_8);

        final ByteBuffer buffer=ByteBuffer.allocate(Integer.BYTES + dataBytes.length);
        buffer.putInt(dataBytes.length);
        buffer.put(dataBytes);

        return addVersionToBytes(buffer.array());
    }

    public@NotNull BlockData deserializeSync(int version, byte[] bytes) {
        if(version==1){
            final ByteBuffer buffer=ByteBuffer.wrap(bytes);

            final byte[]dataBytes=new byte[buffer.getInt()];
            buffer.get(dataBytes);
            return Bukkit.createBlockData(new String(dataBytes));
        } else throw createUnsupportedVersionException(version);
    }
}