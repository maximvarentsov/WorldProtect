package ru.gtncraft.worldprotect;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.gtncraft.worldprotect.Listeners.*;
import ru.gtncraft.worldprotect.Region.Flags;
import ru.gtncraft.worldprotect.Region.Players;
import ru.gtncraft.worldprotect.Region.Region;
import java.io.IOException;

final public class WorldProtect extends JavaPlugin {

    private RegionManager rm;

    final public String PERMISSION_ADMIN = "worldprotect.admin";
    final public String PERMISSION_USE = "worldprotect.use";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        try {
            rm = new RegionManager(getConfig().getConfigurationSection("db"));
            for (World world : Bukkit.getServer().getWorlds()) {
                getRegionManager().load(world);
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
            getRegionManager().save(world);
        }
    }
    /**
     * API stuff.
     *
     */
    public boolean prevent(final Location location, final Player player, final Flags.prevent flag) {
        if (player.hasPermission(PERMISSION_ADMIN)) {
            return false;
        }
        for (Region region : getRegionManager().get(location)) {
            if (region.is(flag) && region.is(player, Players.role.guest)) {
                return true;
            }
        }
        return false;
    }

    public boolean prevent(final Location location, final Flags.prevent flag) {
        for (Region region : getRegionManager().get(location)) {
            if (region.is(flag)) {
                return true;
            }
        }
        return false;
    }

    public RegionManager getRegionManager() {
        return rm;
    }
}
