package ru.gtncraft.worldprotect.listeners;

import ru.gtncraft.worldprotect.WorldProtect;

public class Listeners {

    public Listeners(final WorldProtect plugin) {
        new BlockListener(plugin);
        new EntityListener(plugin);
        new PlayerListener(plugin);
        new VehicleListener(plugin);
        new HandingListener(plugin);
        new WorldListener(plugin);
    }
}
