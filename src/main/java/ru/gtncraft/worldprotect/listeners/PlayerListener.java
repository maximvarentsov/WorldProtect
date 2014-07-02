package ru.gtncraft.worldprotect.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import ru.gtncraft.worldprotect.Config;
import ru.gtncraft.worldprotect.Messages;
import ru.gtncraft.worldprotect.ProtectionManager;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.flags.Prevent;

class PlayerListener implements Listener {

    final ProtectionManager manager;
    final Config config;

    public PlayerListener(final WorldProtect plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.manager = plugin.getProtectionManager();
        this.config = plugin.getConfig();
    }
    /**
     * Called when a player interacts with an object or air.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onInteract(final PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Location location = event.getClickedBlock().getLocation();
        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK:
                if (config.getInfoTool() == event.getMaterial()) {
                    manager.get(location).ifPresent(
                            v -> v.forEach(region -> player.sendMessage(config.getMessage(region)))
                    );
                }
                if (manager.prevent(location, player, event.getClickedBlock().getType())) {
                    event.setCancelled(true);
                    player.sendMessage(config.getMessage(Messages.error_region_protected));
                    return;
                }
                if (manager.prevent(location, player, event.getItem())) {
                    event.setCancelled(true);
                    player.sendMessage(config.getMessage(Messages.error_region_protected));
                    return;
                }
                break;
            case LEFT_CLICK_BLOCK:
                // dont't allow extinguish fire.
                if (Material.FIRE.equals(event.getClickedBlock().getRelative(event.getBlockFace()).getType())) {
                    if (manager.prevent(location, player, Prevent.build)) {
                        event.setCancelled(true);
                        player.sendMessage(config.getMessage(Messages.error_region_protected));
                    }
                }
                break;
            case PHYSICAL:
                if (manager.prevent(location, player, Prevent.use)) {
                    event.setCancelled(true);
                    player.sendMessage(config.getMessage(Messages.error_region_protected));
                }
                break;
        }
    }
    /**
     * This event is fired when the player is almost about to enter the bed.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onBedEnter(final PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        if (manager.prevent(event.getBed().getLocation(), player, Prevent.use)) {
            event.setCancelled(true);
            player.sendMessage(config.getMessage(Messages.error_region_protected));
        }
    }
    /**
     * Called when a player fill a Bucket.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onBucketFill(final PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        if (manager.prevent(event.getBlockClicked().getLocation(), player, Prevent.build)) {
            event.setCancelled(true);
            player.sendMessage(config.getMessage(Messages.error_region_protected));
        }
    }
    /**
     * Called when a player empty a Bucket.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onBucketEmpty(final PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (manager.prevent(event.getBlockClicked().getLocation(), player, Prevent.build)) {
            event.setCancelled(true);
            player.sendMessage(config.getMessage(Messages.error_region_protected));
        }
    }
    /**
     * Represents an event that is called when a player right clicks an entity.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onInteractEntity(final PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (manager.prevent(event.getRightClicked().getLocation(), player, Prevent.build)) {
            event.setCancelled(true);
            player.sendMessage(config.getMessage(Messages.error_region_protected));
        }
    }
    /**
     * Called early in the command handling process. This event is only for very exceptional
     * cases and you should not normally use it.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().substring(1).split(" ")[0];
        if (manager.prevent(player.getLocation(), player, command)) {
            event.setCancelled(true);
            player.sendMessage(config.getMessage(Messages.error_command_disabled, command));
        }
    }
    /**
     * Holds information for player teleport events.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onTeleport(final PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (manager.prevent(player.getLocation(), player, Prevent.teleport)) {
            event.setCancelled(true);
            player.sendMessage(config.getMessage(Messages.error_region_protected));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onBukkitEmpty(final PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        switch (event.getBucket().toString()) {
            case "LAVA_BUCKET":
                if (manager.prevent(player.getLocation(), player, Prevent.bukkitEmptyLava)) {
                    player.sendMessage(config.getMessage(Messages.error_region_protected));
                    event.setCancelled(true);
                }
                break;
            case "WATER_BUCKET":
                if (manager.prevent(player.getLocation(), player, Prevent.bukkitEmptyWater)) {
                    player.sendMessage(config.getMessage(Messages.error_region_protected));
                    event.setCancelled(true);
                }
                break;
        }
    }
    /**
     * Called when a player is about to teleport because it is in contact with a portal.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onPlayerPortal(final PlayerPortalEvent event) {
        if (manager.prevent(event.getTo(), Prevent.portalCreation)) {
            event.useTravelAgent(false);
        }
    }
}
