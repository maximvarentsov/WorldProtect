package ru.gtncraft.worldprotect.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import ru.gtncraft.worldprotect.*;
import ru.gtncraft.worldprotect.Region.Flags;

import java.util.List;

public class PlayerListener implements Listener {

    private final RegionManager manager;
    private final List<String> preventCommands;
    private final List<Material> preventUse;
    private final Material tool;
    private final Config config;

    public PlayerListener(final WorldProtect plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.manager = plugin.getRegionManager();
        this.preventCommands = plugin.getConfig().getPreventCommands();
        this.preventUse = plugin.getConfig().getPreventUse();
        this.tool = plugin.getConfig().getInfoTool();
        this.config = plugin.getConfig();
    }

    private boolean preventCommand(final Player player, final String message) {
        if (manager.hasAccess(player)) {
            return false;
        }
        String command = message.substring(1).split(" ")[0];
        return preventCommands.contains(command.toLowerCase());
    }
    /**
     * Called when a player interacts with an object or air.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(final PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Location location = event.getClickedBlock().getLocation();
        boolean prevent = false;
        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK:
                if (preventUse.contains(event.getClickedBlock().getType())) {
                    prevent = true;
                } else if (event.getItem() != null && event.getItem().getType() == tool) {
                    player.sendMessage(config.getMessage(manager.get(location)));
                }
                break;
            case LEFT_CLICK_BLOCK:
                if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                    Block type = event.getClickedBlock().getRelative(event.getBlockFace());
                    if (type.getType().equals(Material.FIRE)) {
                        prevent = true;
                    }
                }
                break;
            case PHYSICAL:
                prevent = true;
                break;
        }
        if (prevent && manager.prevent(location, player, Flags.prevent.use)) {
            event.setCancelled(true);
            player.sendMessage(config.getMessage(Messages.error_region_protected));
        }
    }
    /**
     * This event is fired when the player is almost about to enter the bed.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBedEnter(final PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        if (manager.prevent(event.getBed().getLocation(), player, Flags.prevent.use)) {
            event.setCancelled(true);
            player.sendMessage(config.getMessage(Messages.error_region_protected));
        }
    }
    /**
     * Called when a player fill a Bucket.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketFill(final PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        if (manager.prevent(event.getBlockClicked().getLocation(), player, Flags.prevent.build)) {
            event.setCancelled(true);
            player.sendMessage(config.getMessage(Messages.error_region_protected));
        }
    }
    /**
     * Called when a player empty a Bucket.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketEmpty(final PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (manager.prevent(event.getBlockClicked().getLocation(), player, Flags.prevent.build)) {
            event.setCancelled(true);
            player.sendMessage(config.getMessage(Messages.error_region_protected));
        }
    }
    /**
     * Represents an event that is called when a player right clicks an entity.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractEntity(final PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (manager.prevent(event.getRightClicked().getLocation(), player, Flags.prevent.build)) {
            event.setCancelled(true);
            player.sendMessage(config.getMessage(Messages.error_region_protected));
        }
    }
    /**
     * Called early in the command handling process. This event is only for very exceptional
     * cases and you should not normally use it.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (manager.prevent(player.getLocation(), player, Flags.prevent.command) || preventCommand(player, event.getMessage())) {
            event.setCancelled(true);
            player.sendMessage(config.getMessage(Messages.error_region_protected));
        }
    }
    /**
     * Holds information for player teleport events.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(final PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            if (manager.prevent(player.getLocation(), player, Flags.prevent.teleport)) {
                event.setCancelled(true);
                player.sendMessage(config.getMessage(Messages.error_region_protected));
            }
        }
    }
}
