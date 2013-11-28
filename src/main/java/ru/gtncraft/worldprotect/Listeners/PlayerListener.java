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
import ru.gtncraft.worldprotect.Region.Region;
import ru.gtncraft.worldprotect.WorldProtect;

import java.util.List;

final public class PlayerListener implements Listener {

    final private WorldProtect plugin;
    final private Material item;
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

        String item = plugin.getConfig().getConfigurationSection("region").getString("item", "STICK");
        this.item = Material.matchMaterial(item);
        if (this.item == null) {
            plugin.getLogger().severe("Region item not found " + item);
        }
    }

    private void checkForRegion(Location location, Player player) {
        List<Region> regions = plugin.getRegionManager().get(location);
        if (regions.size() == 0) {
            player.sendMessage(Lang.REGION_NOT_FOUND_IN_AREA);
        }
        for (Region region : regions) {
            Lang.showRegionInfo(player, region);
        }
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
                } else if (event.getItem() != null && event.getItem().getType() == item) {
                    checkForRegion(location, player);
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
            player.sendMessage(Lang.REGION_NO_PERMISSION);
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
            player.sendMessage(Lang.REGION_NO_PERMISSION);
        }
    }
    /**
     * Called when a player fill a Bucket.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketFill(final PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        if (plugin.prevent(event.getBlockClicked().getLocation(), player, Flags.prevent.build)) {
            event.setCancelled(true);
            player.sendMessage(Lang.REGION_NO_PERMISSION);
        }
    }
    /**
     * Called when a player empty a Bucket.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketEmpty(final PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (plugin.prevent(event.getBlockClicked().getLocation(), player, Flags.prevent.build)) {
            event.setCancelled(true);
            player.sendMessage(Lang.REGION_NO_PERMISSION);
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
            player.sendMessage(Lang.REGION_NO_PERMISSION);
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
            player.sendMessage(Lang.REGION_NO_PERMISSION);
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
                player.sendMessage(Lang.REGION_NO_PERMISSION);
            }
        }
    }
}
