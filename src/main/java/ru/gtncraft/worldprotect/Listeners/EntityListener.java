package ru.gtncraft.worldprotect.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
     * Called when a LivingEntity shoots a bow firing an arrow.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShootBow(final EntityShootBowEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            Player player = (Player) event.getEntity();
            if (manager.prevent(player.getLocation(), player, Prevent.damage)) {
                event.setCancelled(true);
                player.sendMessage(config.getMessage(Messages.error_region_protected));
            }
        } else {
            event.setCancelled(
                manager.prevent(event.getEntity().getLocation(), Prevent.damage)
            );
        }
    }
    /**
     * Thrown when a non-player entity (such as an Enderman) tries to teleport from one location to another.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(final EntityTeleportEvent event) {
        event.setCancelled(
            manager.prevent(event.getTo(), Prevent.teleport) || manager.prevent(event.getFrom(), Prevent.teleport)
        );
    }
    /**
     * Called when an entity explodes.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplode(final EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            if (manager.prevent(block.getLocation(), Prevent.explode)) {
                event.setCancelled(true);
                break;
            }
        }
    }
    /**
     * Called when an entity has made a decision to explode.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplosionPrime(final ExplosionPrimeEvent event) {
        event.setCancelled(
            manager.prevent(event.getEntity().getLocation(), Prevent.explode)
        );
    }
    /**
     * Called when a creature is spawned into a world.
     * If a Creature Spawn event is cancelled, the creature will not spawn.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreatureSpawn(final CreatureSpawnEvent event) {
        if (manager.prevent(event.getLocation(), Prevent.creatureSpawn)) {
            event.setCancelled(true);
        }
    }
    /**
     * Called when any Entity, excluding players, changes a block.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChangeBlock(final EntityChangeBlockEvent event) {
        event.setCancelled(
            manager.prevent(event.getBlock().getLocation(), Prevent.build)
        );
    }
    /**
     * Called when an entity is damaged by a block.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamageByBlock(final EntityDamageByBlockEvent event) {
        event.setCancelled(
            manager.prevent(event.getEntity().getLocation(), Prevent.damage)
        );
    }

    /**
     * Stores data for damage events.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(final EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (manager.prevent(entity.getLocation(), Prevent.damage)) {
            event.setCancelled(true);
        }
    }
    /**
     * Called when an entity is damaged by an entity.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamageByEntity(final EntityDamageByEntityEvent event) {
        final Entity target = event.getEntity();
        final Entity damager = event.getDamager();
        if (damager instanceof Player) {
            final Player player = (Player) damager;

            if (manager.prevent(target.getLocation(), player, Prevent.damage)) {
                event.setCancelled(true);
                player.sendMessage(config.getMessage(Messages.error_region_protected));
                return;
            }

            if (target instanceof Player) {
                if (manager.prevent(target.getLocation(), player, Prevent.pvp)) {
                    event.setCancelled(true);
                    player.sendMessage(config.getMessage(Messages.error_region_protected));
                    return;
                }
            }
        } else {
            event.setCancelled(
                manager.prevent(target.getLocation(), Prevent.damage)
            );
        }
    }
}
