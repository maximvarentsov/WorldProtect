package ru.gtncraft.worldprotect;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.gtncraft.worldprotect.database.JsonFile;
import ru.gtncraft.worldprotect.database.MongoDB;
import ru.gtncraft.worldprotect.database.Storage;
import ru.gtncraft.worldprotect.flags.Prevent;
import ru.gtncraft.worldprotect.region.Cuboid;
import ru.gtncraft.worldprotect.region.Region;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RegionManager {

    private final Storage storage;
    private final WorldProtect plugin;
    private final Map<String, Map<String, Region>> regions;
    private final Map<String, Table<Integer, Integer, Collection<Region>>> chunks;
    private final Collection<String> preventCommands;
    private final Collection<Material> preventUse;

    public RegionManager(final WorldProtect plugin) throws IOException {
        this.plugin = plugin;
        this.regions = new HashMap<>();
        this.chunks = new HashMap<>();
        switch (plugin.getConfig().getStorage()) {
            case mongodb:
                this.storage = new MongoDB(plugin);
                break;
            case file:
                this.storage = new JsonFile(plugin);
                break;
            default:
                throw new IOException("Unknown regions storage.");
        }
        for (final World world : Bukkit.getServer().getWorlds()) {
            load(world);
        }
        this.preventCommands = plugin.getConfig().getPreventCommands();
        this.preventUse = plugin.getConfig().getPreventUse();
    }
    /**
     * Load regions for world.
     *
     * @param world World
     */
    public void load(final World world) {
        final Map<String, Region> values = new HashMap<>();
        final Table<Integer, Integer, Collection<Region>> table = HashBasedTable.create();
        if (plugin.getConfig().useRegions(world)) {
            for (final Region region : storage.load(world)) {
                for (final Map.Entry<Integer, Integer> entry : region.getCuboid().getChunksCoords().entries()) {
                    final int x = entry.getKey();
                    final int z = entry.getValue();
                    if (!table.contains(x, z)) {
                        table.put(x, z, new ArrayList<Region>());
                    }
                    table.get(x, z).add(region);
                }
                values.put(region.getName(), region);
            }
        }
        regions.put(world.getName(), values);
        chunks.put(world.getName(), table);
    }
    /**
     * Delete region from world.
     *
     * @param world World
     * @param name region name
     */
    public void delete(final World world, final String name) {
        final Region region = get(world, name.toLowerCase());
        storage.delete(world, name);
        for (final Map.Entry<Integer, Integer> entry : region.getCuboid().getChunksCoords().entries()) {
            final int x = entry.getKey();
            final int z = entry.getValue();
            final Collection<Region> coll = chunks.get(world.getName()).get(x, z);
            coll.remove(region);
            if (coll.size() == 0) {
                chunks.get(world.getName()).remove(x, z);
            }
        }
        get(world).remove(name.toLowerCase());
    }
    /**
     * Save world regions.
     *
     * @param world World.
     */
    public void save(final World world) {
        if (plugin.getConfig().useRegions(world)) {
            plugin.getLogger().info("Save region for world " + world.getName());
            storage.save(world, get(world).values());
        }
    }
    /**
     * Save all worlds regions and close storage.
     */
    public void disable() {
        for (final World world : Bukkit.getWorlds()) {
            save(world);
        }
        try {
            storage.close();
        } catch (Exception ex) {
            plugin.getLogger().severe(ex.getMessage());
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
        chunks.remove(world.getName());
    }
    /**
     * Add new region in world.
     *
     * @param world World.
     * @param region region.
     */
    public void add(final World world, final Region region) {
        get(world).put(region.getName(), region);
        final Table<Integer, Integer, Collection<Region>> table = chunks.get(world.getName());
        for (final Map.Entry<Integer, Integer> entry : region.getCuboid().getChunksCoords().entries()) {
            final int x = entry.getKey();
            final int z = entry.getValue();
            if (!table.contains(x, z)) {
                table.put(x, z, new ArrayList<Region>());
            }
            table.get(x, z).add(region);
        }
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
        final Collection<Region> result = new ArrayList<>();
        final Cuboid cuboid = value.getCuboid();
        for (final Region region : get(cuboid.getWorld()).values()) {
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
        final Collection<Region> result = new ArrayList<>();
        final Chunk chunk = location.getChunk();
        final Collection<Region> regions = chunks.get(location.getWorld().getName()).get(chunk.getX(), chunk.getZ());
        if (regions != null) {
            for (Region region : regions) {
                if (region.contains(location)) {
                    result.add(region);
                }
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
        final Collection<Region> result = new ArrayList<>();
        for (final Region region : get(player.getWorld()).values()) {
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
        final Collection<Region> regions = get(player.getLocation());
        // Player not in region.
        if (regions.isEmpty()) {
            return true;
        }
        for (final Region region : regions) {
            if (region.contains(player.getName())) {
                return true;
            }
        }
        return false;
    }
    /**
     * If player is region guest and command not allowed in region for guests.
     */
    public boolean prevent(final Location location, final Player player, final String command) {
        if (prevent(location, player, Prevent.command)) {
            return true;
        }
        if (hasAccess(player)) {
            return false;
        }
        return preventCommands.contains(command.toLowerCase());
    }
    /**
     * Prevent guests use some items like bone meal.
     */
    public boolean prevent(final Location location, final Player player, final ItemStack item) {
        if (item != null) {
            if (item.getType().equals(Material.INK_SACK) && item.getDurability() == 15) {
                return prevent(location, player, Prevent.use);
            }
        }
        return false;
    }
    /**
     * Check guest can use some material.
     */
    public boolean prevent(final Location location, final Player player, final Material material) {
        if (preventUse.contains(material)) {
            return prevent(location, player, Prevent.use);
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
        for (final Region region : get(location)) {
            if (region.contains(location)) {
                if (region.get(flag) && !region.contains(player.getName())) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * Search any region inside location with true prevent flag.
     */
    public boolean prevent(final Location location, final Prevent flag) {
        for (final Region region: get(location)) {
            if (region.contains(location)) {
                if (region.get(flag)) {
                    return true;
                }
            }
        }
        return false;
    }
}
