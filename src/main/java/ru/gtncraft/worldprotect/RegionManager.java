package ru.gtncraft.worldprotect;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.gtncraft.worldprotect.database.JsonFile;
import ru.gtncraft.worldprotect.database.MongoDB;
import ru.gtncraft.worldprotect.database.Storage;
import ru.gtncraft.worldprotect.flags.Prevent;
import ru.gtncraft.worldprotect.region.Cuboid;
import ru.gtncraft.worldprotect.region.Region;
import java.io.IOException;
import java.util.*;

public class RegionManager {

    private final Storage storage;
    private final WorldProtect plugin;
    private final Map<String, Map<String, Region>> regions = new HashMap<>();

    public RegionManager(final WorldProtect plugin) throws IOException {
        this.plugin = plugin;
        switch (plugin.getConfig().getString("storage.type")) {
            case "mongodb":
                this.storage = new MongoDB(plugin);
                break;
            case "file":
                this.storage = new JsonFile(plugin);
                break;
            default:
                throw new IOException("Unknown regions storage.");
        }
        for (World world : Bukkit.getServer().getWorlds()) {
            load(world);
        }
    }
    /**
     * Load regions for world.
     *
     * @param world World
     */
    public void load(final World world) {
        Map<String, Region> values = new HashMap<>();
        if (plugin.getConfig().useRegions(world)) {
            values = storage.load(world);
        }
        regions.put(world.getName(), values);
    }
    /**
     * Delete region from world.
     *
     * @param world World
     * @param name region name
     */
    public void delete(final World world, final String name) {
        get(world).remove(name);
        storage.delete(world, name);
    }
    /**
     * Save world regions.
     *
     * @param world World.
     */
    public void save(final World world) {
        storage.save(world, get(world));
    }
    /**
     * Save all worlds regions.
     *
     */
    public void saveAll() {
        for (World world : Bukkit.getWorlds()) {
            save(world);
        }
    }
    /**
     * Save and unload world regions.
     *
     * @param world World.
     */
    public void unload(final World world) {
        save(world);
        regions.remove(world.getName());
    }
    /**
     * Add new region in world.
     *
     * @param world World.
     * @param region region.
     */
    public void add(final World world, final Region region) {
        get(world).put(region.getName(), region);
    }
    /**
     * Get world regions.
     *
     * @param world World.
     */
    public Map<String, Region> get(final World world) {
        return regions.get(world.getName());
    }
    /**
     * Get regions overlays.
     *
     * @param value region.
     */
    public Collection<Region> getOverlays(final Region value) {
        Collection<Region> result = new ArrayList<>();
        final Cuboid cuboid = value.getCuboid();
        for (Region region : get(cuboid.getWorld()).values()) {
            if (region.contains(cuboid.getLowerNE()) || region.contains(cuboid.getUpperSW()) || // inside
                cuboid.contains(region.getCuboid().getLowerNE()) || cuboid.contains(region.getCuboid().getUpperSW())) { // outside
                result.add(region);
            }
        }
        return result;
    }
    /**
     * Get regions inside current location.
     *
     * @param location Current location.
     */
    public Collection<Region> get(final Location location) {
        Collection<Region> result = new ArrayList<>();
        for (Region region: get(location.getWorld()).values()) {
            if (region.contains(location)) {
                result.add(region);
            }
        }
        return result;
    }
    /**
     * Get region in current world by name.
     *
     * @param world World.
     * @param name region name.
     */
    public Region get(final World world, final String name) {
        return get(world).get(name.toLowerCase());
    }
    /**
     * Return regions owned by player in current world.
     *
     * @param player Player.
     * @param role Player Roles in region.
     */
    public Collection<Region> get(final Player player, final Roles role) {
        Collection<Region> result = new ArrayList<>();
        for (Region region : get(player.getWorld()).values()) {
            if (region.contains(player, role)) {
                result.add(region);
            }
        }
        return result;
    }
    /**
     * Check player member or owner of region.
     *
     * @param player Player
     */
    public boolean hasAccess(final Player player) {
        if (player.hasPermission(Permissions.admin)) {
            return true;
        }
        Collection<Region> regions = get(player.getLocation());
        // Player not in region.
        if (regions.isEmpty()) {
            return true;
        }
        for (Region region : regions) {
            if (region.contains(player.getName())) {
                return true;
            }
        }
        return false;
    }
    /**
     * Check player is owner/member of any region inside location with true prevent flag.
     */
    public boolean prevent(final Location location, final Player player, final Prevent flag) {
        if (player.hasPermission(Permissions.admin) || player.hasPermission(Permissions.moder)) {
            return false;
        }
        for (Region region : get(location)) {
            if (region.get(flag) && (!region.contains(player.getName()))) {
                return true;
            }
        }
        return false;
    }
    /**
     * Search any region inside location with true prevent flag.
     */
    public boolean prevent(final Location location, final Prevent flag) {
        for (Region region : get(location)) {
            if (region.get(flag)) {
                return true;
            }
        }
        return false;
    }
}
