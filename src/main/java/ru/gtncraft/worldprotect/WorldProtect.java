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
import ru.gtncraft.worldprotect.database.JsonFile;
import ru.gtncraft.worldprotect.database.MongoDB;
import ru.gtncraft.worldprotect.database.Storage;

import java.io.IOException;

public class WorldProtect extends JavaPlugin {

    private RegionManager manager;

    public final String PERMISSION_ADMIN = "worldprotect.admin";
    public final String PERMISSION_USE = "worldprotect.use";

    @Override
    public void onEnable() {

        saveDefaultConfig();
        Storage storage;

        try {
            switch (getConfig().getString("storage.type", "file")) {
                case "mongodb":
                    storage = new MongoDB(this);
                    break;
                case "file":
                    storage = new JsonFile(this);
                    break;
                default:
                    throw new IOException("Unknown regions storage.");
            }
        } catch (IOException ex) {
            getLogger().severe(ex.getMessage());
            setEnabled(false);
            return;
        }

        manager = new RegionManager(storage);

        for (World world : Bukkit.getServer().getWorlds()) {
            getLogger().info("Load regions for world " + world.getName() + ".");
            getRegionManager().load(world);
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
        for (World world : Bukkit.getWorlds()) {
            getLogger().info("Save regions for world " + world.getName() + ".");
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
            if (region.has(flag) && region.has(player, Players.role.guest)) {
                return true;
            }
        }
        return false;
    }

    public boolean prevent(final Location location, final Flags.prevent flag) {
        for (Region region : getRegionManager().get(location)) {
            if (region.has(flag)) {
                return true;
            }
        }
        return false;
    }

    public RegionManager getRegionManager() {
        return manager;
    }
}
