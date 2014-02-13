package ru.gtncraft.worldprotect;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.gtncraft.worldprotect.Region.Cuboid;
import ru.gtncraft.worldprotect.Region.Region;
import ru.gtncraft.worldprotect.database.JsonFile;
import ru.gtncraft.worldprotect.database.MongoDB;
import ru.gtncraft.worldprotect.database.Storage;
import ru.gtncraft.worldprotect.flags.Prevent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionManager {

    private final Storage storage;
    private final Map<String, Map<String, Region>> regions = new HashMap<>();

    public RegionManager(final WorldProtect plugin) throws IOException {

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
        // Load all worlds which use regions.
        for (World world : Bukkit.getServer().getWorlds()) {
            if (plugin.getConfig().useRegions(world)) {
                load(world);
            }
        }
    }
    /**
     * Load regions for world.
     *
     * @param world World
     */
    public void load(final World world) {
        regions.put(world.getName(), storage.load(world));
    }
    /**
     * Delete region from world.
     *
     * @param world World
     * @param name Region name
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
     * @param region Region.
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
    public List<Region> getOverlays(final Region value) {
        List<Region> result = new ArrayList<>();
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
    public List<Region> get(final Location location) {
        List<Region> result = new ArrayList<>();
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
     * @param name Region name.
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
    public List<Region> get(final Player player, final Roles role) {
        ArrayList<Region> result = new ArrayList<>();
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
        List<Region> regions = get(player.getLocation());
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
        if (player.hasPermission(Permissions.admin)) {
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
