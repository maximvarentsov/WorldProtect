package ru.gtncraft.worldprotect.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import ru.gtncraft.worldprotect.Config;
import ru.gtncraft.worldprotect.Messages;
import ru.gtncraft.worldprotect.ProtectionManager;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.flags.Prevent;

class VehicleListener implements Listener {

    final ProtectionManager manager;
    final Config config;

    public VehicleListener(final WorldProtect plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.manager = plugin.getProtectionManager();
        this.config = plugin.getConfig();
    }
    /**
     * Raised when a vehicle is destroyed, which could be caused by either a player or the environment.
     * This is not raised if the boat is simply 'removed' due to other means.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onDestory(final VehicleDestroyEvent event) {
        Entity attacker = event.getAttacker();
        if (attacker instanceof Player) {
            Player player = (Player) attacker;
            if (manager.prevent(event.getVehicle().getLocation(), player, Prevent.build)) {
                event.setCancelled(true);
                player.sendMessage(config.getMessage(Messages.error_region_protected));
            }
        } else {
            if (manager.prevent(event.getVehicle().getLocation(), Prevent.vehicleNaturalDestroy)) {
                if (manager.prevent(event.getVehicle().getLocation(), Prevent.build)) {
                    event.setCancelled(true);
                }
            }
        }
    }
    /**
     * Raised when an entity enters a vehicle.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onEnter(final VehicleEnterEvent event) {
        Entity passenger = event.getEntered().getPassenger();
        if (passenger instanceof Player) {
            Player player = (Player) passenger;
            if (manager.prevent(event.getVehicle().getLocation(), player, Prevent.use)) {
                event.setCancelled(true);
                player.sendMessage(config.getMessage(Messages.error_region_protected));
            }
        }
    }
}
