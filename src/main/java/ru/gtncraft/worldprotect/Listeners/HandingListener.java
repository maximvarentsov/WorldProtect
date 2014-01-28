package ru.gtncraft.worldprotect.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import ru.gtncraft.worldprotect.Lang;
import ru.gtncraft.worldprotect.Region.Flags;
import ru.gtncraft.worldprotect.WorldProtect;

public class HandingListener implements Listener {

    private final WorldProtect plugin;

    public HandingListener(final WorldProtect plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }
    /**
     * Triggered when a hanging entity is removed by an entity.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreakByEntityEvent(final HangingBreakByEntityEvent event) {
        Location location = event.getEntity().getLocation();
        if (event.getRemover().getType() == EntityType.PLAYER) {
            Player player = (Player) event.getRemover();
            if (plugin.prevent(location, player, Flags.prevent.build)) {
                event.setCancelled(true);
                player.sendMessage(Lang.REGION_NO_PERMISSION);
            }
        } else {
            event.setCancelled(
                plugin.prevent(location, Flags.prevent.build)
            );
        }
    }
    /**
     * Triggered when a hanging entity is created in the world.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(final HangingPlaceEvent event) {
        Player player = event.getPlayer();
        if (plugin.prevent(event.getEntity().getLocation(), player, Flags.prevent.use)) {
            event.setCancelled(true);
            player.sendMessage(Lang.REGION_NO_PERMISSION);
        }
    }
}
