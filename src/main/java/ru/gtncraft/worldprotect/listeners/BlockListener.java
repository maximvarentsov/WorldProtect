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
import ru.gtncraft.worldprotect.ProtectionManager;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.flags.Prevent;

class BlockListener implements Listener {

    private final ProtectionManager manager;
    private final Config config;

    public BlockListener(final WorldProtect plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.manager = plugin.getProtectionManager();
        this.config = plugin.getConfig();
    }
    /**
     * Called when a block is broken by a player.
     *
     * If a Block Break event is cancelled, the block will not break and experience will not drop.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    @SuppressWarnings("unused")
    public void onBreak(final BlockBreakEvent event) {
        Player player = event.getPlayer();
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
    @SuppressWarnings("unused")
    public void onPlace(final BlockPlaceEvent event) {
        Player player = event.getPlayer();
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
    @SuppressWarnings("unused")
    public void onIgnite(final BlockIgniteEvent event) {
        Player player = event.getPlayer();
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
    @SuppressWarnings("unused")
    public void onSignChange(final SignChangeEvent event) {
        Player player = event.getPlayer();
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
    @SuppressWarnings("unused")
    public void onBurn(final BlockBurnEvent event) {
        if (manager.prevent(event.getBlock().getLocation(), Prevent.burn)) {
            event.setCancelled(true);
        }
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
    @SuppressWarnings("unused")
    public void onFade(final BlockFadeEvent event) {
        if (manager.prevent(event.getBlock().getLocation(), Prevent.fade)) {
            event.setCancelled(true);
        }
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
    @SuppressWarnings("unused")
    public void onForm(final BlockFormEvent event) {
        if (manager.prevent(event.getBlock().getLocation(), Prevent.grow)) {
            event.setCancelled(true);
        }
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
    @SuppressWarnings("unused")
    public void onSpread(final BlockSpreadEvent event) {
        if (manager.prevent(event.getBlock().getLocation(), Prevent.grow)) {
            event.setCancelled(true);
        }
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
    @SuppressWarnings("unused")
    public void onGrow(final BlockGrowEvent event) {
        if (manager.prevent(event.getBlock().getLocation(), Prevent.grow)) {
            event.setCancelled(true);
        }
    }
    /**
     * Called when leaves are decaying naturally.
     *
     * If a Leaves Decay event is cancelled, the leaves will not decay.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    @SuppressWarnings("unused")
    public void onLeavesDecay(final LeavesDecayEvent event) {
        if (manager.prevent(event.getBlock().getLocation(), Prevent.leavesDecay)) {
            event.setCancelled(true);
        }
    }
    /**
     * Called when a block is formed by entities.
     *
     * Examples:
     *  - Snow formed by a Snowman.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    @SuppressWarnings("unused")
    public void onEntityBlockForm(final EntityBlockFormEvent event) {
        if (manager.prevent(event.getBlock().getLocation(), Prevent.build)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    @SuppressWarnings("unused")
    public void onPistonEvent(final BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (manager.prevent(block.getLocation(), Prevent.piston)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    @SuppressWarnings("unused")
    public void onPistonEvent(final BlockPistonRetractEvent event) {
        if (manager.prevent(event.getRetractLocation(), Prevent.piston)) {
            event.setCancelled(true);
        }
    }
}
