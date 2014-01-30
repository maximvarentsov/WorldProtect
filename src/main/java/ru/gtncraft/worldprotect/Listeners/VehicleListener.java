package ru.gtncraft.worldprotect.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import ru.gtncraft.worldprotect.Lang;
import ru.gtncraft.worldprotect.Region.Flags;
import ru.gtncraft.worldprotect.RegionManager;
import ru.gtncraft.worldprotect.WorldProtect;

public class VehicleListener implements Listener {

    private final RegionManager manager;

    public VehicleListener(final WorldProtect plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.manager = plugin.getRegionManager();
    }
    /**
     * Raised when a vehicle is destroyed, which could be caused by either a player or the environment.
     * This is not raised if the boat is simply 'removed' due to other means.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDestory(final VehicleDestroyEvent event) {
        Entity attacker = event.getAttacker();
        if (attacker instanceof Player) {
            Player player = (Player) attacker;
            if (manager.prevent(event.getVehicle().getLocation(), player, Flags.prevent.build)) {
                event.setCancelled(true);
                player.sendMessage(Lang.REGION_NO_PERMISSION);
            }
        } else {
            event.setCancelled(
                manager.prevent(event.getVehicle().getLocation(), Flags.prevent.build)
            );
        }
    }
    /**
     * Raised when an entity enters a vehicle.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnter(final VehicleEnterEvent event) {
        Entity passenger = event.getEntered().getPassenger();
        if (passenger instanceof Player) {
            Player player = (Player) passenger;
            if (manager.prevent(event.getVehicle().getLocation(), player, Flags.prevent.use)) {
                event.setCancelled(true);
                player.sendMessage(Lang.REGION_NO_PERMISSION);
            }
        }
    }
}
