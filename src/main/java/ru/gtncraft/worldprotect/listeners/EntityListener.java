package ru.gtncraft.worldprotect.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import ru.gtncraft.worldprotect.Config;
import ru.gtncraft.worldprotect.Messages;
import ru.gtncraft.worldprotect.ProtectionManager;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.flags.Prevent;

public class EntityListener implements Listener {

    private final ProtectionManager manager;
    private final Config config;

    public EntityListener(final WorldProtect plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.manager = plugin.getProtectionManager();
        this.config = plugin.getConfig();
    }
    /**
     * Thrown when a non-player entity (such as an Enderman) tries to teleport from one location to another.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    @SuppressWarnings("unused")
    public void onTeleport(final EntityTeleportEvent event) {
        if (manager.prevent(event.getTo(), Prevent.teleport) || manager.prevent(event.getFrom(), Prevent.teleport)) {
            event.setCancelled(true);
        }
    }
    /**
     * Called when an entity explodes.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    @SuppressWarnings("unused")
    public void onExplode(final EntityExplodeEvent event) {
        if (manager.prevent(event.getLocation(), Prevent.explode)) {
            event.setCancelled(true);
        }
    }
    /**
     * Called when an entity has made a decision to explode.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    @SuppressWarnings("unused")
    public void onExplosionPrime(final ExplosionPrimeEvent event) {
        if (manager.prevent(event.getEntity().getLocation(), Prevent.explode)) {
            event.setCancelled(true);
        }
    }
    /**
     * Called when a creature is spawned into a world.
     * If a Creature Spawn event is cancelled, the creature will not spawn.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    @SuppressWarnings("unused")
    public void onCreatureSpawn(final CreatureSpawnEvent event) {
        if (manager.prevent(event.getLocation(), Prevent.creatureSpawn)) {
            event.setCancelled(true);
        }
    }
    /**
     * Called when any Entity, excluding players, changes a block.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    @SuppressWarnings("unused")
    public void onChangeBlock(final EntityChangeBlockEvent event) {
        if (event.getEntityType() == EntityType.FALLING_BLOCK) {
            if (manager.prevent(event.getBlock().getLocation(), Prevent.fallingBlocks)) {
                event.setCancelled(true);
            }
        } else {
            if (manager.prevent(event.getBlock().getLocation(), Prevent.build)) {
                event.setCancelled(true);
            }
        }
    }
    /**
     * Stores data for damage events.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    @SuppressWarnings("unused")
    public void onDamage(final EntityDamageEvent event) {
        Entity target = event.getEntity();
        if (event instanceof EntityDamageByEntityEvent) {
            Entity attacker = ((EntityDamageByEntityEvent) event).getDamager();
            if (target instanceof Player) {
                if (attacker instanceof Player) {
                    Player player = (Player) attacker;
                    if (manager.prevent(target.getLocation(), player, Prevent.pvp)) {
                        event.setCancelled(true);
                        player.sendMessage(config.getMessage(Messages.error_pvp_disabled));
                    }
                    return;
                } else if (attacker instanceof Arrow) {
                    Arrow arrow = (Arrow) attacker;
                    if (arrow.getShooter() instanceof Player) {
                        Player player = (Player) arrow.getShooter();
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
