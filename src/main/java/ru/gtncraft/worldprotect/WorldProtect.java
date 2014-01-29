package ru.gtncraft.worldprotect;

import org.bukkit.plugin.java.JavaPlugin;
import ru.gtncraft.worldprotect.Listeners.*;

import java.io.IOException;

public final class WorldProtect extends JavaPlugin {

    private RegionManager manager;
    private Config config;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        config = new Config(super.getConfig());

        try {
            manager = new RegionManager(this);
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

        new Commands(this);
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        getRegionManager().saveAll();
    }
    /**
     * API
     */
    public RegionManager getRegionManager() {
        return manager;
    }
}
