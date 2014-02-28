package ru.gtncraft.worldprotect.database;

import org.bukkit.World;
import ru.gtncraft.worldprotect.region.Region;

import java.util.Collection;

public interface Storage extends AutoCloseable {
    void save(final World world, final Collection<Region> regions);
    void delete(final World world, final String name);
    Collection<Region> load(final World world);
}
