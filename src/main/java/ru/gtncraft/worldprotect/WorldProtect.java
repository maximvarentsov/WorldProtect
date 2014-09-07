package ru.gtncraft.worldprotect;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import ru.gtncraft.worldprotect.commands.CommandRegion;
import ru.gtncraft.worldprotect.commands.CommandWorldProtect;
import ru.gtncraft.worldprotect.listeners.*;

import java.io.IOException;

public final class WorldProtect extends JavaPlugin {

    private ProtectionManager manager;
    private boolean emergency = false;

    @Override
    public void onLoad() {
        saveDefaultConfig();
        manager = new ProtectionManager(this);
        Messages.load(getConfig().getConfigurationSection("messages"));
    }

    @Override
    public void onEnable() {
        for (World world : Bukkit.getServer().getWorlds()) {
            try {
                getProtectionManager().load(world);
            } catch (Throwable ex) {
                getLogger().severe(ex.getMessage());
                emergency = true;
            }
        }

        new CommandRegion(this);
        new CommandWorldProtect(this);

        if (emergency) {
            new EmergencyListeners(this);
        } else {
            new BlockListener(this);
            new EntityListener(this);
            new HandingListener(this);
            new PlayerListener(this);
            new VehicleListener(this);
            new WorldListener(this);
        }
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        for (World world : Bukkit.getWorlds()) {
            try {
                getProtectionManager().save(world);
            } catch (IOException ex) {
                getLogger().severe(ex.getMessage());
            }
        }
    }

    public ProtectionManager getProtectionManager() {
        return manager;
    }
}
