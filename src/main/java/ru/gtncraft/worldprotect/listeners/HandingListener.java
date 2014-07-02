package ru.gtncraft.worldprotect.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import ru.gtncraft.worldprotect.Config;
import ru.gtncraft.worldprotect.Messages;
import ru.gtncraft.worldprotect.ProtectionManager;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.flags.Prevent;

class HandingListener implements Listener {

    final ProtectionManager manager;
    final Config config;

    public HandingListener(final WorldProtect plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.manager = plugin.getProtectionManager();
        this.config = plugin.getConfig();
    }
    /**
     * Triggered when a hanging entity is removed by an entity.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onBreakByEntityEvent(final HangingBreakByEntityEvent event) {
        Location location = event.getEntity().getLocation();
        if (event.getRemover().getType() == EntityType.PLAYER) {
            Player player = (Player) event.getRemover();
            if (manager.prevent(location, player, Prevent.build)) {
                event.setCancelled(true);
                player.sendMessage(config.getMessage(Messages.error_region_protected));
            }
        } else {
            if (manager.prevent(location, Prevent.build)) {
                event.setCancelled(true);
            }
        }
    }
    /**
     * Triggered when a hanging entity is created in the world.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onPlace(final HangingPlaceEvent event) {
        Player player = event.getPlayer();
        if (manager.prevent(event.getEntity().getLocation(), player, Prevent.use)) {
            event.setCancelled(true);
            player.sendMessage(config.getMessage(Messages.error_region_protected));
        }
    }
}
