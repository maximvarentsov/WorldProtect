package ru.gtncraft.worldprotect;

import org.bukkit.plugin.java.JavaPlugin;
import ru.gtncraft.worldprotect.commands.CommandWorldProtect;
import ru.gtncraft.worldprotect.listeners.*;
import ru.gtncraft.worldprotect.commands.CommandRegion;

import java.io.IOException;

public final class WorldProtect extends JavaPlugin {

    private ProtectionManager manager;
    private Config config;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        config = new Config(super.getConfig());

        try {
            manager = new ProtectionManager(this);
        } catch (IOException ex) {
            getLogger().severe(ex.getMessage());
            setEnabled(false);
            return;
        }

        new BlockListener(this);
        new EntityListener(this);
        new PlayerListener(this);
        new VehicleListener(this);
        new HandingListener(this);
        new WorldListener(this);

        new CommandWorldProtect(this);
        new CommandRegion(this);
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public void onDisable() {
        getProtectionManager().disable();
        getServer().getScheduler().cancelTasks(this);
    }
    /**
     * API
     */
    public ProtectionManager getProtectionManager() {
        return manager;
    }
}
