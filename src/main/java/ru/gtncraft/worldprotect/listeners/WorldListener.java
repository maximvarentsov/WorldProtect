package ru.gtncraft.worldprotect.listeners;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.flags.Prevent;

import java.io.IOException;

class WorldListener implements Listener {

    final WorldProtect plugin;

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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.getProtectionManager().load(event.getWorld());
            } catch (IOException ex) {
                plugin.getLogger().severe(ex.getMessage());
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    @SuppressWarnings("unused")
    public void onUnload(final WorldUnloadEvent event) {
        plugin.getProtectionManager().unload(event.getWorld());
    }

    @EventHandler(ignoreCancelled = true)
    @SuppressWarnings("unused")
    public void onPortalCreate(final PortalCreateEvent event) {
        for (Block block : event.getBlocks()) {
            if (plugin.getProtectionManager().prevent(block.getLocation(), Prevent.portalCreation)) {
                event.setCancelled(true);
                break;
            }
        }
    }
}
