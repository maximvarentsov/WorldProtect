package ru.gtncraft.worldprotect;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.gtncraft.worldprotect.Listeners.*;
import ru.gtncraft.worldprotect.Region.Flags;
import ru.gtncraft.worldprotect.Region.Region;
import ru.gtncraft.worldprotect.Region.Regions;

import java.io.IOException;
import java.util.Map;

final public class WorldProtect extends JavaPlugin {

    private RegionManager rm;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        try {

            rm = new RegionManager(getConfig().getConfigurationSection("db"));

            for (World world : Bukkit.getServer().getWorlds()) {
                rm.load(world.getName());
            }

            new BlockListener(this);
            new EntityListener(this);
            new PlayerListener(this);
            new VehicleListener(this);
            new HandingListener(this);
            new WorldListener(this);

            new Commands(this);

        } catch (IOException ex) {
            getLogger().severe(ex.getMessage());
            setEnabled(false);
        }
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        for (World world : Bukkit.getWorlds()) {
            getRegionManager().save(world.getName());
        }
    }

    /**
     * API stuff.
     *
     */

    public boolean prevent(final Location location, final Player player, final Flags.prevent flag) {
        for (Region region : getRegionManager().getRegions(location)) {
            if (region.flags.get(flag) && region.isGuest(player)) {
                return true;
            }
        }
        return false;
    }

    public boolean prevent(final Location location, final Flags.prevent flag) {
        for (Region region : getRegionManager().getRegions(location)) {
            if (region.flags.get(flag)) {
                return true;
            }
        }
        return false;
    }

    public RegionManager getRegionManager() {
        return rm;
    }
}
