package ru.gtncraft.worldprotect.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import ru.gtncraft.worldprotect.WorldProtect;

final public class WorldListener implements Listener {

    final private WorldProtect plugin;

    public WorldListener(WorldProtect plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSave(final WorldSaveEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                String world = event.getWorld().getName();
                plugin.getLogger().fine("Begin world saving " + world + ".");
                plugin.getRegionManager().save(world);
                plugin.getLogger().fine("End world saving for " + world + ".");
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onLoad(final WorldLoadEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getRegionManager().load(event.getWorld().getName());
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onUnload(final WorldUnloadEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getRegionManager().unload(event.getWorld().getName());
            }
        });
    }
}
