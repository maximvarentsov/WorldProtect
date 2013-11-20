package ru.gtncraft.worldprotect.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import ru.gtncraft.worldprotect.Lang;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.Region.Flags;

final public class EntityListener implements Listener {

    final private WorldProtect plugin;

    public EntityListener(WorldProtect plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }
    /**
     * Called when a LivingEntity shoots a bow firing an arrow.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShootBow(EntityShootBowEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            Player player = (Player) event.getEntity();
            if (plugin.prevent(player.getLocation(), player, Flags.prevent.damage)) {
                event.setCancelled(true);
                player.sendMessage(Lang.REGION_NO_PERMISSION);
            }
        } else {
            event.setCancelled(
                plugin.prevent(event.getEntity().getLocation(), Flags.prevent.damage)
            );
        }
    }
    /**
     * Thrown when a non-player entity (such as an Enderman) tries to teleport from one location to another.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(EntityTeleportEvent event) {
        event.setCancelled(
            plugin.prevent(event.getTo(), Flags.prevent.teleport) || plugin.prevent(event.getFrom(), Flags.prevent.teleport)
        );
    }
    /**
     * Called when an entity explodes.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplode(final EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            if (plugin.prevent(block.getLocation(), Flags.prevent.explode)) {
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
            plugin.prevent(event.getEntity().getLocation(), Flags.prevent.explode)
        );
    }
    /**
     * Called when a creature is spawned into a world.
     * If a Creature Spawn event is cancelled, the creature will not spawn.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreatureSpawn(final CreatureSpawnEvent event) {
        if (plugin.prevent(event.getLocation(), Flags.prevent.creatureSpawn)) {
            event.setCancelled(true);
        }
    }
    /**
     * Called when any Entity, excluding players, changes a block.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChangeBlock(final EntityChangeBlockEvent event) {
        event.setCancelled(
            plugin.prevent(event.getBlock().getLocation(), Flags.prevent.build)
        );
    }
    /**
     * Called when an entity is damaged by a block.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamageByBlock(final EntityDamageByBlockEvent event) {
        event.setCancelled(
            plugin.prevent(event.getEntity().getLocation(), Flags.prevent.damage)
        );
    }
    /**
     * Called when an entity is damaged by an entity.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamageByEntity(final EntityDamageByEntityEvent event) {
        Entity target = event.getEntity();
        Entity damager = event.getDamager();
        if (damager.getType() == EntityType.PLAYER) {
            Player player = (Player) damager;
            if (plugin.prevent(target.getLocation(), player, Flags.prevent.damage)) {
                event.setCancelled(true);
                player.sendMessage(Lang.REGION_NO_PERMISSION);
            }
        } else {
            event.setCancelled(
                plugin.prevent(target.getLocation(), Flags.prevent.damage)
            );
        }
    }
}
