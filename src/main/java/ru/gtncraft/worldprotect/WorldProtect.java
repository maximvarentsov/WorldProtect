package ru.gtncraft.worldprotect;

import org.bukkit.plugin.java.JavaPlugin;
import ru.gtncraft.worldprotect.Listeners.*;

import java.io.IOException;

public final class WorldProtect extends JavaPlugin {

    private RegionManager manager;

    @Override
    public void onEnable() {

        saveDefaultConfig();

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
        return new Config(super.getConfig());
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
