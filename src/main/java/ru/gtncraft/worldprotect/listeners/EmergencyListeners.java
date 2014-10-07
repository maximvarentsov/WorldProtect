package ru.gtncraft.worldprotect.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.gtncraft.worldprotect.Message;
import ru.gtncraft.worldprotect.Translations;
import ru.gtncraft.worldprotect.WorldProtect;

public class EmergencyListeners implements Listener {

    public EmergencyListeners(final WorldProtect plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    void onBlockBreak(final BlockBreakEvent event) {
        event.getPlayer().sendMessage(Translations.get(Message.error_emergency));
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    void onBlockPlace(final BlockPlaceEvent event) {
        event.getPlayer().sendMessage(Translations.get(Message.error_emergency));
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    void onPlayerInteract(final PlayerInteractEvent event) {
        event.getPlayer().sendMessage(Translations.get(Message.error_emergency));
        event.setCancelled(true);
    }
}
