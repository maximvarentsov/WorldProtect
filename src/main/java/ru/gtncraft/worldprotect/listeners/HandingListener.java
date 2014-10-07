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
import ru.gtncraft.worldprotect.*;
import ru.gtncraft.worldprotect.region.Flag;

public class HandingListener implements Listener {
    private final ProtectionManager manager;

    public HandingListener(final WorldProtect plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        manager = plugin.getProtectionManager();
    }
    /**
     * Triggered when a hanging entity is removed by an entity.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onBreakByEntityEvent(final HangingBreakByEntityEvent event) {
        Location location = event.getEntity().getLocation();
        if (event.getRemover().getType() == EntityType.PLAYER) {
            Player player = (Player) event.getRemover();
            if (manager.prevent(location, player, Flag.build)) {
                event.setCancelled(true);
                player.sendMessage(Messages.get(Message.error_region_protected));
            }
        } else {
            if (manager.prevent(location, Flag.build)) {
                event.setCancelled(true);
            }
        }
    }
    /**
     * Triggered when a hanging entity is created in the world.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onPlace(final HangingPlaceEvent event) {
        Player player = event.getPlayer();
        if (manager.prevent(event.getEntity().getLocation(), player, Flag.use)) {
            event.setCancelled(true);
            player.sendMessage(Messages.get(Message.error_region_protected));
        }
    }
}
