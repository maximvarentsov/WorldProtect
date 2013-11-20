package ru.gtncraft.worldprotect.Listeners;

import com.google.common.collect.ImmutableList;
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
import ru.gtncraft.worldprotect.Lang;
import ru.gtncraft.worldprotect.Region.Flags;
import ru.gtncraft.worldprotect.WorldProtect;

final public class PlayerListener implements Listener {

    final private WorldProtect plugin;

    final private ImmutableList<Material> materialsProtected = ImmutableList.of(
            Material.CHEST,
            Material.LOCKED_CHEST,
            Material.DISPENSER,
            Material.DROPPER,
            Material.WORKBENCH,
            Material.JUKEBOX,
            Material.BREWING_STAND,
            Material.ANVIL,
            Material.CAULDRON,
            Material.FURNACE,
            Material.DROPPER,
            Material.ENCHANTMENT_TABLE,
            Material.STONE_BUTTON,
            Material.WOOD_BUTTON,
            Material.WOOD_DOOR,
            Material.IRON_DOOR,
            Material.TRAP_DOOR,
            Material.ENDER_CHEST,
            Material.BEACON,
            Material.WOODEN_DOOR
    );

    public PlayerListener(WorldProtect plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
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
                if (materialsProtected.contains(event.getClickedBlock().getType())) {
                    prevent = true;
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
        if (prevent && plugin.prevent(location, player, Flags.prevent.use)) {
            event.setCancelled(true);
            player.sendMessage(Lang.REGION_IS_PROTECTED);
        }

    }
    /**
     * This event is fired when the player is almost about to enter the bed.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBedEnter(final PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        if (plugin.prevent(event.getBed().getLocation(), player, Flags.prevent.use)) {
            event.setCancelled(true);
            player.sendMessage(Lang.REGION_IS_PROTECTED);
        }
    }
    /**
     * Called when a player interacts with a Bucket.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucket(final PlayerBucketEvent event) {
        Player player = event.getPlayer();
        if (plugin.prevent(event.getBlockClicked().getLocation(), player, Flags.prevent.build)) {
            event.setCancelled(true);
            player.sendMessage(Lang.REGION_IS_PROTECTED);
        }
    }
    /**
     * Represents an event that is called when a player right clicks an entity.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractEntity(final PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (plugin.prevent(event.getRightClicked().getLocation(), player, Flags.prevent.build)) {
            event.setCancelled(true);
            player.sendMessage(Lang.REGION_IS_PROTECTED);
        }
    }
    /**
     * Called early in the command handling process. This event is only for very exceptional
     * cases and you should not normally use it.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (plugin.prevent(player.getLocation(), player, Flags.prevent.command)) {
            event.setCancelled(true);
            player.sendMessage(Lang.REGION_IS_PROTECTED);
        }
    }
    /**
     * Holds information for player teleport events.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(final PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            if (plugin.prevent(player.getLocation(), player, Flags.prevent.teleport)) {
                event.setCancelled(true);
                player.sendMessage(Lang.REGION_IS_PROTECTED);
            }
        }
    }
}
