package ru.gtncraft.worldprotect.database;

import org.bukkit.World;
import ru.gtncraft.worldprotect.Region.Region;
import java.util.Map;

public interface Storage {
    void save(final World world, final Map<String, Region> regions);
    void delete(final World world, final String name);
    Map<String, Region> load(final World world);
}
