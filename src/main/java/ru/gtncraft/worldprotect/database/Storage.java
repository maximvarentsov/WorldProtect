package ru.gtncraft.worldprotect.database;

import org.bukkit.World;
import ru.gtncraft.worldprotect.region.Region;
import java.util.Map;

public interface Storage extends AutoCloseable {
    void save(final World world, final Map<String, Region> regions);
    void delete(final World world, final String name);
    Map<String, Region> load(final World world);
}
