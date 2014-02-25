package ru.gtncraft.worldprotect.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import ru.gtncraft.worldprotect.Config;
import ru.gtncraft.worldprotect.Messages;
import ru.gtncraft.worldprotect.RegionManager;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.flags.Prevent;
import java.util.Collection;

public class PlayerListener implements Listener {

    private final RegionManager manager;
    private final Collection<String> preventCommands;
    private final Collection<Material> preventUse;
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
        final String command = message.substring(1).split(" ")[0];
        return preventCommands.contains(command.toLowerCase());
    }
    /**
     * Called when a player interacts with an object or air.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Location location = event.getClickedBlock().getLocation();
        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK:
                if (preventUse.contains(event.getClickedBlock().getType())) {
                    if (manager.prevent(location, player, Prevent.use)) {
                        event.setCancelled(true);
                        player.sendMessage(config.getMessage(Messages.error_region_protected));
                    }
                } else if (event.getItem() != null) {
                    final ItemStack itemStack = event.getItem();
                    if (tool.equals(itemStack.getType())) {
                        player.sendMessage(config.getMessage(manager.get(location)));
                        event.setCancelled(true);
                    } else if (Material.INK_SACK.equals(itemStack.getType()) && itemStack.getDurability() == 15) {
                        if (manager.prevent(location, player, Prevent.grow)) {
                            event.setCancelled(true);
                            player.sendMessage(config.getMessage(Messages.error_region_protected));
                        }
                    }
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
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBedEnter(final PlayerBedEnterEvent event) {
        final Player player = event.getPlayer();
        if (manager.prevent(event.getBed().getLocation(), player, Prevent.use)) {
            event.setCancelled(true);
            player.sendMessage(config.getMessage(Messages.error_region_protected));
        }
    }
    /**
     * Called when a player fill a Bucket.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBucketFill(final PlayerBucketFillEvent event) {
        final Player player = event.getPlayer();
        if (manager.prevent(event.getBlockClicked().getLocation(), player, Prevent.build)) {
            event.setCancelled(true);
            player.sendMessage(config.getMessage(Messages.error_region_protected));
        }
    }
    /**
     * Called when a player empty a Bucket.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBucketEmpty(final PlayerBucketEmptyEvent event) {
        final Player player = event.getPlayer();
        if (manager.prevent(event.getBlockClicked().getLocation(), player, Prevent.build)) {
            event.setCancelled(true);
            player.sendMessage(config.getMessage(Messages.error_region_protected));
        }
    }
    /**
     * Represents an event that is called when a player right clicks an entity.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInteractEntity(final PlayerInteractEntityEvent event) {
        final Player player = event.getPlayer();
        if (manager.prevent(event.getRightClicked().getLocation(), player, Prevent.build)) {
            event.setCancelled(true);
            player.sendMessage(config.getMessage(Messages.error_region_protected));
        }
    }
    /**
     * Called early in the command handling process. This event is only for very exceptional
     * cases and you should not normally use it.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        if (manager.prevent(player.getLocation(), player, Prevent.command) || preventCommand(player, event.getMessage())) {
            event.setCancelled(true);
            player.sendMessage(config.getMessage(Messages.error_command_disabled, event.getMessage()));
        }
    }
    /**
     * Holds information for player teleport events.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onTeleport(final PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        if (manager.prevent(player.getLocation(), player, Prevent.teleport)) {
            event.setCancelled(true);
            player.sendMessage(config.getMessage(Messages.error_region_protected));
        }
    }
}
