package ru.gtncraft.worldprotect.storage;

import org.bukkit.World;

public interface Storage extends AutoCloseable {
    void save(final World world, final ProtectedWorld data);
    void delete(final World world, final String name);
    ProtectedWorld load(final World world);
}
