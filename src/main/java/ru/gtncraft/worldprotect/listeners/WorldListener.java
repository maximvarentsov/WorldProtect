package ru.gtncraft.worldprotect.listeners;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.region.Flag;

import java.io.IOException;

public class WorldListener implements Listener {

    private final WorldProtect plugin;

    public WorldListener(final WorldProtect plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    @SuppressWarnings("unused")
    void onLoad(final WorldLoadEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    plugin.getProtectionManager().load(event.getWorld());
                } catch (IOException ex) {
                    plugin.getLogger().severe(ex.getMessage());
                }
            }
        });
    }

    @EventHandler
    @SuppressWarnings("unused")
    void onUnload(final WorldUnloadEvent event) {
        plugin.getProtectionManager().unload(event.getWorld());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onPortalCreate(final PortalCreateEvent event) {
        for (Block block : event.getBlocks()) {
            if (plugin.getProtectionManager().prevent(block.getLocation(), Flag.portalCreation)) {
                event.setCancelled(true);
                break;
            }
        }
    }
}
