package ru.gtncraft.worldprotect.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import ru.gtncraft.worldprotect.Lang;
import ru.gtncraft.worldprotect.Region.Flags;
import ru.gtncraft.worldprotect.WorldProtect;

final public class BlockListener implements Listener {

    final private WorldProtect plugin;

    public BlockListener(WorldProtect plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }
    /**
     * Called when a block is broken by a player.
     *
     * If a Block Break event is cancelled, the block will not break and experience will not drop.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(final BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (plugin.prevent(event.getBlock().getLocation(), player, Flags.prevent.build)) {
            event.setCancelled(true);
            player.sendMessage(Lang.REGION_NO_PERMISSION);
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
        if (plugin.prevent(event.getBlock().getLocation(), player, Flags.prevent.build)) {
            event.setCancelled(true);
            player.sendMessage(Lang.REGION_NO_PERMISSION);
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
            if (plugin.prevent(event.getBlock().getLocation(), player, Flags.prevent.burn)) {
                event.setCancelled(true);
                player.sendMessage(Lang.REGION_NO_PERMISSION);
            }
        } else {
            if (plugin.prevent(event.getBlock().getLocation(), Flags.prevent.burn)) {
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
        if (plugin.prevent(event.getBlock().getLocation(), player, Flags.prevent.use)) {
            event.setCancelled(true);
            player.sendMessage(Lang.REGION_NO_PERMISSION);
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
            plugin.prevent(event.getBlock().getLocation(), Flags.prevent.burn)
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
            plugin.prevent(event.getBlock().getLocation(), Flags.prevent.fade)
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
            plugin.prevent(event.getBlock().getLocation(), Flags.prevent.grow)
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
            plugin.prevent(event.getBlock().getLocation(), Flags.prevent.grow)
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
            plugin.prevent(event.getBlock().getLocation(), Flags.prevent.grow)
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
            plugin.prevent(event.getBlock().getLocation(), Flags.prevent.leavesDecay)
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
            plugin.prevent(event.getBlock().getLocation(), Flags.prevent.build)
        );
    }
    /**
     * Represents events with a source block and a destination block, currently only applies to liquid (lava and water)
     * and teleporting dragon eggs.
     *
     * If a Block From To event is cancelled, the block will not move (the liquid will not flow).
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFromToEvent(final BlockFromToEvent event) {
        event.setCancelled(
            plugin.prevent(event.getToBlock().getLocation(), Flags.prevent.build)
        );
    }
    */
}
