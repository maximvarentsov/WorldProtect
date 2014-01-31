package ru.gtncraft.worldprotect.Listeners;

import org.bukkit.Bukkit;
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
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onIgnite(final BlockIgniteEvent event) {
        Player player = event.getPlayer();
        if (player instanceof Player) {
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
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityBlockForm(final EntityBlockFormEvent event) {
        event.setCancelled(
            manager.prevent(event.getBlock().getLocation(), Prevent.build)
        );
    }
}
