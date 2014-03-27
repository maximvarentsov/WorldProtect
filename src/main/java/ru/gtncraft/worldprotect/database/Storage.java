package ru.gtncraft.worldprotect.database;

import org.bukkit.World;
import ru.gtncraft.worldprotect.region.Region;

import java.util.stream.Stream;

public interface Storage extends AutoCloseable {
    void save(final World world, final Stream<Region> regions);
    void delete(final World world, final String name);
    Stream<Region> load(final World world);
}
