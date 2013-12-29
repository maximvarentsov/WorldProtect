package ru.gtncraft.worldprotect.database;

import org.bukkit.World;
import ru.gtncraft.worldprotect.Region.Region;
import java.util.Map;

public interface Storage {
    void save(World world, Map<String, Region> regions);
    void delete(World world, String name);
    Map<String, Region> load(World world);
}
