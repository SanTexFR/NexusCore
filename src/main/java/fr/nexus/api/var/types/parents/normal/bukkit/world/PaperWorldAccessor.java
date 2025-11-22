package fr.nexus.api.var.types.parents.normal.bukkit.world;

import org.bukkit.Bukkit;
import org.bukkit.World;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class PaperWorldAccessor implements WorldAccessor{
    @Override
    public World getWorld(String name) {
        return Bukkit.getWorld(name);
    }

    @Override
    public String getName(World world) {
        return world.getName();
    }
}
