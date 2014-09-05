package ru.gtncraft.worldprotect.storage;

import org.bukkit.World;
import ru.gtncraft.worldprotect.region.DataHolder;

import java.io.IOException;

public interface Storage {
    DataHolder load(final World world) throws IOException;
    void save(final World world, final DataHolder data) throws IOException;
    void remove(final World world);
}
