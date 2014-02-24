package ru.gtncraft.worldprotect.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import ru.gtncraft.worldprotect.Config;
import ru.gtncraft.worldprotect.Messages;
import ru.gtncraft.worldprotect.RegionManager;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.flags.Prevent;

public class EntityListener implements Listener {

    private final RegionManager manager;
    private final Config config;

    public EntityListener(final WorldProtect plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.manager = plugin.getRegionManager();
        this.config = plugin.getConfig();
    }
    /**
     * Thrown when a non-player entity (such as an Enderman) tries to teleport from one location to another.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(final EntityTeleportEvent event) {
        event.setCancelled(
            manager.prevent(event.getTo(), Prevent.teleport) || manager.prevent(event.getFrom(), Prevent.teleport)
        );
    }
    /**
     * Called when an entity explodes.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplode(final EntityExplodeEvent event) {
        if (manager.prevent(event.getLocation(), Prevent.explode)) {
            event.setCancelled(true);
        }
    }
    /**
     * Called when an entity has made a decision to explode.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplosionPrime(final ExplosionPrimeEvent event) {
        event.setCancelled(
            manager.prevent(event.getEntity().getLocation(), Prevent.explode)
        );
    }
    /**
     * Called when a creature is spawned into a world.
     * If a Creature Spawn event is cancelled, the creature will not spawn.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(final CreatureSpawnEvent event) {
        if (manager.prevent(event.getLocation(), Prevent.creatureSpawn)) {
            event.setCancelled(true);
        }
    }
    /**
     * Called when any Entity, excluding players, changes a block.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChangeBlock(final EntityChangeBlockEvent event) {
        event.setCancelled(
            manager.prevent(event.getBlock().getLocation(), Prevent.build)
        );
    }
    /**
     * Stores data for damage events.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(final EntityDamageEvent event) {
        final Entity target = event.getEntity();
        if (event instanceof EntityDamageByEntityEvent) {
            final EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
            final Entity damager = ev.getDamager();
            if (target instanceof Player) {
                if (damager instanceof Player) {
                    final Player player = (Player) damager;
                    if (manager.prevent(target.getLocation(), player, Prevent.pvp)) {
                        event.setCancelled(true);
                        player.sendMessage(config.getMessage(Messages.error_pvp_disabled));
                    }
                    return;
                } else if (damager instanceof Arrow) {
                    final Arrow arrow = (Arrow) damager;
                    if (arrow.getShooter() instanceof Player) {
                        final Player player = (Player) arrow.getShooter();
                        if (manager.prevent(target.getLocation(), player, Prevent.pvp)) {
                            event.setCancelled(true);
                            player.sendMessage(config.getMessage(Messages.error_pvp_disabled));
                        }
                        return;
                    }
                }
            }
        }
        if (manager.prevent(target.getLocation(), Prevent.damage)) {
            event.setCancelled(true);
        }
    }
}
