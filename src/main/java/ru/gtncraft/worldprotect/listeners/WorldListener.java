package ru.gtncraft.worldprotect.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import ru.gtncraft.worldprotect.WorldProtect;

class WorldListener implements Listener {

    private final WorldProtect plugin;

    public WorldListener(final WorldProtect plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler()
    @SuppressWarnings("unused")
    public void onSave(final WorldSaveEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getProtectionManager().save(event.getWorld()));
    }

    @EventHandler()
    @SuppressWarnings("unused")
    public void onLoad(final WorldLoadEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getProtectionManager().load(event.getWorld()));
    }

    @EventHandler(ignoreCancelled = true)
    @SuppressWarnings("unused")
    public void onUnload(final WorldUnloadEvent event) {
        plugin.getProtectionManager().unload(event.getWorld());
    }
}
