package ru.gtncraft.worldprotect.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.gtncraft.worldprotect.Messages;
import ru.gtncraft.worldprotect.WorldProtect;

public class EmergencyListeners implements Listener {

    final String message;

    public EmergencyListeners(final WorldProtect plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        message = plugin.getConfig().getMessage(Messages.error_emergency);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    @SuppressWarnings("unused")
    public void onBlockBreak(final BlockBreakEvent event) {
        event.getPlayer().sendMessage(message);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    @SuppressWarnings("unused")
    public void onBlockPlace(final BlockPlaceEvent event) {
        event.getPlayer().sendMessage(message);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    @SuppressWarnings("unused")
    public void onPlayerInteract(final PlayerInteractEvent event) {
        event.getPlayer().sendMessage(message);
    }
}
