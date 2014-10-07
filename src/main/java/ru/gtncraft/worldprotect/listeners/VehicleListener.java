package ru.gtncraft.worldprotect.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import ru.gtncraft.worldprotect.*;
import ru.gtncraft.worldprotect.region.Flag;

public class VehicleListener implements Listener {
    private final ProtectionManager manager;

    public VehicleListener(final WorldProtect plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.manager = plugin.getProtectionManager();
    }
    /**
     * Raised when a vehicle is destroyed, which could be caused by either a player or the environment.
     * This is not raised if the boat is simply 'removed' due to other means.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onDestory(final VehicleDestroyEvent event) {
        Entity attacker = event.getAttacker();
        if (attacker instanceof Player) {
            Player player = (Player) attacker;
            if (manager.prevent(event.getVehicle().getLocation(), player, Flag.build)) {
                event.setCancelled(true);
                player.sendMessage(Messages.get(Message.error_region_protected));
            }
        } else {
            if (manager.prevent(event.getVehicle().getLocation(), Flag.vehicleNaturalDestroy)) {
                if (manager.prevent(event.getVehicle().getLocation(), Flag.build)) {
                    event.setCancelled(true);
                }
            }
        }
    }
    /**
     * Raised when an entity enters a vehicle.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onEnter(final VehicleEnterEvent event) {
        Entity passenger = event.getEntered().getPassenger();
        if (passenger instanceof Player) {
            Player player = (Player) passenger;
            if (manager.prevent(event.getVehicle().getLocation(), player, Flag.use)) {
                event.setCancelled(true);
                player.sendMessage(Messages.get(Message.error_region_protected));
            }
        }
    }
}
