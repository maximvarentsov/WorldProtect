package ru.gtncraft.worldprotect.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.world.PortalCreateEvent;
import ru.gtncraft.worldprotect.Message;
import ru.gtncraft.worldprotect.Messages;
import ru.gtncraft.worldprotect.ProtectionManager;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.region.Flag;

public class EntityListener implements Listener {

    private final ProtectionManager manager;

    public EntityListener(final WorldProtect plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        manager = plugin.getProtectionManager();
    }
    /**
     * Thrown when a non-player entity (such as an Enderman) tries to teleport from one location to another.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    public void onTeleport(final EntityTeleportEvent event) {
        if (manager.prevent(event.getTo(), Flag.teleport) || manager.prevent(event.getFrom(), Flag.teleport)) {
            event.setCancelled(true);
        }
    }
    /**
     * Called when an entity explodes.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onExplode(final EntityExplodeEvent event) {
        if (event.getEntity() == null) {
            return;
        }
        if (manager.prevent(event.getLocation(), Flag.entityBlockExplode) || manager.prevent(event.getLocation(), Flag.explode)) {
            event.getLocation().getWorld().createExplosion(event.getLocation(), 0F);
            event.setCancelled(true);
        }
    }
    /**
     * Called when an entity has made a decision to explode.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onExplosionPrime(final ExplosionPrimeEvent event) {
        Location location = event.getEntity().getLocation();
        if (manager.prevent(location, Flag.entityBlockExplode)|| manager.prevent(location, Flag.explode)) {
            event.setCancelled(true);
        }
    }
    /**
     * Called when a creature is spawned into a world.
     * If a Creature Spawn event is cancelled, the creature will not spawn.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onCreatureSpawn(final CreatureSpawnEvent event) {
        if (manager.prevent(event.getLocation(), Flag.creatureSpawn)) {
            event.setCancelled(true);
        }
    }
    /**
     * Called when any Entity, excluding players, changes a block.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onChangeBlock(final EntityChangeBlockEvent event) {
        if (event.getEntityType() == EntityType.FALLING_BLOCK) {
            if (manager.prevent(event.getBlock().getLocation(), Flag.fallingBlocks)) {
                event.setCancelled(true);
            }
        } else {
            if (manager.prevent(event.getBlock().getLocation(), Flag.build)) {
                event.setCancelled(true);
            }
        }
    }
    /**
     * Stores data for damage events.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onDamage(final EntityDamageEvent event) {
        Entity target = event.getEntity();
        if (event instanceof EntityDamageByEntityEvent) {
            Entity attacker = ((EntityDamageByEntityEvent) event).getDamager();
            if (target instanceof Player) {
                if (prevent(attacker, target, Flag.pvp, Message.error_pvp_disabled)) {
                    event.setCancelled(true);
                    return;
                }
            } else if (target instanceof ItemFrame) {
                if (prevent(attacker, target, Flag.use, Message.error_region_protected)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        if (manager.prevent(target.getLocation(), Flag.damage)) {
            event.setCancelled(true);
        }
    }
    /**
     * Called when a non-player entity is about to teleport because it is in contact with a portal.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onPortalCreate(final PortalCreateEvent event) {
        for (Block block : event.getBlocks()) {
            if (manager.prevent(event.getBlocks().get(0).getLocation(), Flag.portalCreation)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    boolean prevent(final Entity attacker, final Entity target, final Flag flag, final Message message) {
        if (attacker instanceof Player) {
            Player player = (Player) attacker;
            if (manager.prevent(target.getLocation(), player, flag)) {
                player.sendMessage(Messages.get(message));
                return true;
            }
        } else if (attacker instanceof Arrow) {
            Arrow arrow = (Arrow) attacker;
            if (arrow.getShooter() instanceof Player) {
                Player player = (Player) arrow.getShooter();
                if (manager.prevent(target.getLocation(), player, flag)) {
                    player.sendMessage(Messages.get(message));
                    return true;
                }
            }
        }
        return false;
    }
}
