package ru.gtncraft.worldprotect.listeners;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import ru.gtncraft.worldprotect.Config;
import ru.gtncraft.worldprotect.Messages;
import ru.gtncraft.worldprotect.RegionManager;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.flags.Prevent;

public class BlockListener implements Listener {

    private final RegionManager manager;
    private final Config config;

    public BlockListener(final WorldProtect plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.manager = plugin.getRegionManager();
        this.config = plugin.getConfig();
    }
    /**
     * Called when a block is broken by a player.
     *
     * If a Block Break event is cancelled, the block will not break and experience will not drop.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBreak(final BlockBreakEvent event) {
        final Player player = event.getPlayer();
        if (manager.prevent(event.getBlock().getLocation(), player, Prevent.build)) {
            event.setCancelled(true);
            player.sendMessage(config.getMessage(Messages.error_region_protected));
        }
    }
    /**
     * Called when a block is placed by a player.
     *
     * If a Block Place event is cancelled, the block will not be placed.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlace(final BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        if (manager.prevent(event.getBlock().getLocation(), player, Prevent.build)) {
            event.setCancelled(true);
            player.sendMessage(config.getMessage(Messages.error_region_protected));
        }
    }
    /**
     * Called when a block is ignited.
     * If you want to catch when a Player places fire, you need to use BlockPlaceEvent.
     *
     * If a Block Ignite event is cancelled, the block will not be ignited.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onIgnite(final BlockIgniteEvent event) {
        final Player player = event.getPlayer();
        if (player != null) {
            if (manager.prevent(event.getBlock().getLocation(), player, Prevent.burn)) {
                event.setCancelled(true);
                player.sendMessage(config.getMessage(Messages.error_region_protected));
            }
        } else {
            if (manager.prevent(event.getBlock().getLocation(), Prevent.burn)) {
                event.setCancelled(true);
            }
        }
    }
    /**
     * Called when a sign is changed by a player.
     *
     * If a Sign Change event is cancelled, the sign will not be changed.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignChange(final SignChangeEvent event) {
        final Player player = event.getPlayer();
        if (manager.prevent(event.getBlock().getLocation(), player, Prevent.use)) {
            event.setCancelled(true);
            player.sendMessage(config.getMessage(Messages.error_region_protected));
        }
    }
    /**
     * Called when a block is destroyed as a result of being burnt by fire.
     *
     * If a Block Burn event is cancelled, the block will not be destroyed as a result of being burnt by fire.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBurn(final BlockBurnEvent event) {
        event.setCancelled(
            manager.prevent(event.getBlock().getLocation(), Prevent.burn)
        );
    }
    /**
     * Called when a block fades, melts or disappears based on world conditions
     *
     * Examples:
     *  - Snow melting due to being near a light source.
     *  - Ice melting due to being near a light source.
     *
     * If a Block Fade event is cancelled, the block will not fade, melt or disappear.
     */
    @EventHandler(ignoreCancelled = true)
    public void onFade(final BlockFadeEvent event) {
        event.setCancelled(
            manager.prevent(event.getBlock().getLocation(), Prevent.fade)
        );
    }
    /**
     * Called when a block is formed or spreads based on world conditions. Use BlockSpreadEvent to catch blocks that
     * actually spread and don't just "randomly" form.
     *
     * Examples:
     *  - Snow forming due to a snow storm.
     *  - Ice forming in a snowy Biome like Taiga or Tundra.
     *
     * If a Block Form event is cancelled, the block will not be formed.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onForm(final BlockFormEvent event) {
        event.setCancelled(
            manager.prevent(event.getBlock().getLocation(), Prevent.grow)
        );
    }
    /**
     * Called when a block spreads based on world conditions. Use BlockFormEvent to catch blocks that "randomly"
     * form instead of actually spread.
     *
     * Examples:
     *  - Mushrooms spreading.
     *  - Fire spreading.
     *
     * If a Block Spread event is cancelled, the block will not spread.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSpread(final BlockSpreadEvent event) {
        event.setCancelled(
            manager.prevent(event.getBlock().getLocation(), Prevent.grow)
        );
    }
    /**
     * Called when a block grows naturally in the world.
     *
     * Examples:
     *  - Wheat
     *  - Sugar Cane
     *  - Cactus
     *  - Watermelon
     *  - Pumpkin
     *
     * If a Block Grow event is cancelled, the block will not grow.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onGrow(final BlockGrowEvent event) {
        event.setCancelled(
            manager.prevent(event.getBlock().getLocation(), Prevent.grow)
        );
    }
    /**
     * Called when leaves are decaying naturally.
     *
     * If a Leaves Decay event is cancelled, the leaves will not decay.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onLeavesDecay(final LeavesDecayEvent event) {
        event.setCancelled(
            manager.prevent(event.getBlock().getLocation(), Prevent.leavesDecay)
        );
    }
    /**
     * Called when a block is formed by entities.
     *
     * Examples:
     *  - Snow formed by a Snowman.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityBlockForm(final EntityBlockFormEvent event) {
        event.setCancelled(
            manager.prevent(event.getBlock().getLocation(), Prevent.build)
        );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPistonEvent(final BlockPistonExtendEvent event) {
        for (final Block block : event.getBlocks()) {
            if (manager.prevent(block.getLocation(), Prevent.piston)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPistonEvent(final BlockPistonRetractEvent event) {
        if (manager.prevent(event.getRetractLocation(), Prevent.piston)) {
            event.setCancelled(true);
        }
    }
}
