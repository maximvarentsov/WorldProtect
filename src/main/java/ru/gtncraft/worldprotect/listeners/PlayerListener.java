package ru.gtncraft.worldprotect.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import ru.gtncraft.worldprotect.Message;
import ru.gtncraft.worldprotect.Translations;
import ru.gtncraft.worldprotect.ProtectionManager;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.region.RegionCube;
import ru.gtncraft.worldprotect.region.Flag;
import ru.gtncraft.worldprotect.util.Region;

public class PlayerListener implements Listener {
    private final ProtectionManager manager;
    private final Material infoTool;
    private final int maxFoodLevel = 20;

    public PlayerListener(final WorldProtect plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        manager = plugin.getProtectionManager();

        String material = plugin.getConfig().getString("region.tool");
        Material tool = Material.matchMaterial(material.toUpperCase());
        if (tool == null) {
            this.infoTool = Material.STICK;
        } else {
            this.infoTool = tool;
        }
    }
    /**
     * Called when a player interacts with an object or air.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onInteract(final PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() == null) {
            return;
        }
        Location location = event.getClickedBlock().getLocation();

        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK:
                if (infoTool == event.getMaterial()) {
                    for (RegionCube region : manager.get(location)) {
                        player.sendMessage(Region.showInfo(region));
                    }
                }
                if (manager.prevent(location, player, event.getClickedBlock().getType())) {
                    event.setCancelled(true);
                    player.sendMessage(Translations.get(Message.error_region_protected));
                    return;
                }
                if (manager.prevent(location, player, event.getItem())) {
                    event.setCancelled(true);
                    player.sendMessage(Translations.get(Message.error_region_protected));
                    return;
                }
                break;
            case LEFT_CLICK_BLOCK:
                // dont't allow extinguish fire.
                if (Material.FIRE.equals(event.getClickedBlock().getRelative(event.getBlockFace()).getType())) {
                    if (manager.prevent(location, player, Flag.build)) {
                        event.setCancelled(true);
                        event.setUseInteractedBlock(Event.Result.DENY);
                        player.sendMessage(Translations.get(Message.error_region_protected));
                    }
                }
                break;
            case PHYSICAL:
                if (manager.prevent(location, player, Flag.use)) {
                    event.setCancelled(true);
                    player.sendMessage(Translations.get(Message.error_region_protected));
                }
                break;
        }
    }
    /**
     * This event is fired when the player is almost about to enter the bed.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onBedEnter(final PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        if (manager.prevent(event.getBed().getLocation(), player, Flag.use)) {
            event.setCancelled(true);
            player.sendMessage(Translations.get(Message.error_region_protected));
        }
    }
    /**
     * Called when a player fill a Bucket.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onBucketFill(final PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        if (manager.prevent(event.getBlockClicked().getLocation(), player, Flag.build)) {
            event.setCancelled(true);
            player.sendMessage(Translations.get(Message.error_region_protected));
        }
    }
    /**
     * Called when a player empty a Bucket.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onBucketEmpty(final PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (manager.prevent(event.getBlockClicked().getLocation(), player, Flag.build)) {
            event.setCancelled(true);
            player.sendMessage(Translations.get(Message.error_region_protected));
        }
    }
    /**
     * Represents an event that is called when a player right clicks an entity.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onInteractEntity(final PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (manager.prevent(event.getRightClicked().getLocation(), player, Flag.build)) {
            event.setCancelled(true);
            player.sendMessage(Translations.get(Message.error_region_protected));
        }
    }
    /**
     * Called early in the command handling process. This event is only for very exceptional
     * cases and you should not normally use it.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().substring(1).split(" ")[0];
        if (manager.prevent(player.getLocation(), player, command)) {
            event.setCancelled(true);
            player.sendMessage(Translations.get(Message.error_command_disabled, command));
        }
    }
    /**
     * Holds information for player teleport events.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onTeleport(final PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (manager.prevent(player.getLocation(), player, Flag.teleport)) {
            event.setCancelled(true);
            player.sendMessage(Translations.get(Message.error_region_protected));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onBukkitEmpty(final PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        switch (event.getBucket().toString()) {
            case "LAVA_BUCKET":
                if (manager.prevent(player.getLocation(), player, Flag.bukkitEmptyLava)) {
                    player.sendMessage(Translations.get(Message.error_region_protected));
                    event.setCancelled(true);
                }
                break;
            case "WATER_BUCKET":
                if (manager.prevent(player.getLocation(), player, Flag.bukkitEmptyWater)) {
                    player.sendMessage(Translations.get(Message.error_region_protected));
                    event.setCancelled(true);
                }
                break;
        }
    }
    /**
     * Called when a player is about to teleport because it is in contact with a portal.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onPlayerPortal(final PlayerPortalEvent event) {
        if (event.getTo() == null) {
            return;
        }
        if (manager.prevent(event.getTo(), Flag.portalCreation)) {
            event.useTravelAgent(false);
        }
    }
    /**
     * Called when a human entity's food level changes
     */
    @EventHandler(priority = EventPriority.LOWEST)
    @SuppressWarnings("unused")
    void onFoodLevelChange(final FoodLevelChangeEvent event) {
        if (manager.prevent(event.getEntity().getLocation(), Flag.hungry)) {
            int foodLevel = event.getFoodLevel();
            if (foodLevel < maxFoodLevel) {
                event.setFoodLevel(foodLevel + 1);
            }
        }
    }
}
