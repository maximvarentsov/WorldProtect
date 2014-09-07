package ru.gtncraft.worldprotect;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.gtncraft.worldprotect.region.RegionCube;
import ru.gtncraft.worldprotect.region.DataHolder;
import ru.gtncraft.worldprotect.region.Flag;
import ru.gtncraft.worldprotect.storage.Json;
import ru.gtncraft.worldprotect.storage.Storage;

import java.io.IOException;
import java.util.*;

public class ProtectionManager {
    private final Storage storage;
    private final Map<String, Collection<RegionCube>> regions = new HashMap<>();
    private final Map<String, Collection<Flag>> worldFlags = new HashMap<>();
    private final Map<String, Table<Integer, Integer, SortedSet<RegionCube>>> chunks = new HashMap<>();
    private final Collection<String> worlds = new ArrayList<>();
    private final Collection<String> preventCommands = new ArrayList<>();
    private final Collection<Material> preventUse = new ArrayList<>();
    private final Collection<Flag> defaultFlags = new ArrayList<>();

    public ProtectionManager(final WorldProtect plugin) {
        storage = new Json(plugin);
        worlds.addAll(plugin.getConfig().getStringList("region.worlds"));
        preventCommands.addAll(plugin.getConfig().getStringList("region.prevent.commands"));

        for (String value : plugin.getConfig().getStringList("region.prevent.use")) {
            Material material = Material.matchMaterial(value.toUpperCase());
            if (material != null) {
                preventUse.add(material);
            }
        }
        for (String flag : plugin.getConfig().getStringList("region.flags.default")) {
            try {
                defaultFlags.add(Flag.valueOf(flag));
            } catch (IllegalArgumentException ignore) {
            }
        }
    }
    /**
     * Load regions for world.
     *
     * @param world World
     */
    public void load(World world) throws IOException {
        Collection<Flag> flags = new ArrayList<>();
        Collection<RegionCube> values = new ArrayList<>();
        Table<Integer, Integer, SortedSet<RegionCube>> table = HashBasedTable.create();

        if (worlds.contains(world.getName())) {
            DataHolder data = storage.load(world);
            if (data != null) {
                for (RegionCube region : data.getRegions()) {
                    for (Map.Entry<Integer, Integer> entry : region.getChunks().entries()) {
                        int x = entry.getKey();
                        int z = entry.getValue();
                        if (!table.contains(x, z)) {
                            table.put(x, z, new TreeSet<RegionCube>());
                        }
                        table.get(x, z).add(region);
                    }
                }
                flags.addAll(data.getFlags());
            } else {
                flags.addAll(defaultFlags);
            }
        }
        worldFlags.put(world.getName(), flags);
        regions.put(world.getName(), values);
        chunks.put(world.getName(), table);
    }
    /**
     * Delete region from world.
     *
     * @param world World
     * @param name region name
     */
    public void delete(World world, String name) {
        RegionCube region = get(world, name);
        if (region != null) {
            for (Map.Entry<Integer, Integer> entry : region.getChunks().entries()) {
                int x = entry.getKey();
                int z = entry.getValue();
                Collection<RegionCube> coll = chunks.get(world.getName()).get(x, z);
                coll.remove(region);
                if (coll.isEmpty()) {
                    chunks.get(world.getName()).remove(x, z);
                }
            }
            regions.get(world.getName()).remove(region);
        }
    }
    /**
     * Save world regions.
     *
     * @param world World.
     */
    public void save(World world) throws IOException {
        if (worlds.contains(world.getName())) {
            storage.save(world, new DataHolder(get(world), getWorldFlags(world)));
        }
    }
    /**
     * Save and unload world regions.
     *
     * @param world World.
     */
    public void unload(World world) {
        regions.remove(world.getName());
        chunks.remove(world.getName());
        worldFlags.remove(world.getName());
    }
    /**
     * Add new region in world.
     *
     * @param world World.
     * @param region region.
     */
    public void add(World world, RegionCube region) {
        regions.get(world.getName()).add(region);
        Table<Integer, Integer, SortedSet<RegionCube>> table = chunks.get(world.getName());
        for (Map.Entry<Integer, Integer> entry : region.getChunks().entries()) {
            int x = entry.getKey();
            int z = entry.getValue();
            if (!table.contains(x, z)) {
                table.put(x, z, new TreeSet<RegionCube>());
            }
            table.get(x, z).add(region);
        }
    }
    /**
     * Return player own regions.
     *
     * @param player player
     */
    public Collection<RegionCube> getOwn(Player player) {
        Collection<RegionCube> result = new ArrayList<>();
        World world = player.getWorld();
        UUID uuid = player.getUniqueId();
        for (RegionCube region : regions.get(world.getName())) {
            if (region.getOwners().contains(uuid)) {
                result.add(region);
            }
        }
        return result;
    }
    /**
     * Get world regions.
     *
     * @param world World.
     */
    public Collection<RegionCube> get(World world) {
        return regions.get(world.getName());
    }
    /**
     * Get regions inside current location.
     *
     * @param location Current location.
     */
    public Collection<RegionCube> get(Location location) {
        Collection<RegionCube> result = new ArrayList<>();
        Chunk chunk = location.getChunk();
        Collection<RegionCube> regions = chunks.get(location.getWorld().getName()).get(chunk.getX(), chunk.getZ());
        if (regions == null) {
            return result;
        }
        for (RegionCube region : regions) {
            if (region.contains(location)) {
                result.add(region);
            }
        }
        return result;
    }
    /**
     * Get region in world by name.
     *
     * @param world World.
     * @param name region name.
     */
    public RegionCube get(World world, String name) {
        String value = name.toLowerCase();
        for (RegionCube region : get(world)) {
            if (region.getName().equals(value)) {
                return region;
            }
        }
        return null;
    }
    /**
     * Get regions overlays.
     *
     * @param region region.
     */
    public Collection<RegionCube> AABB(World world, RegionCube region) {
        Collection<RegionCube> result = new ArrayList<>();
        for (RegionCube overlay : get(world)) {
            if (region.AABB(overlay)) {
                result.add(overlay);
            }
        }
        return result;
    }
    /**
     * If player is region guest and command not allowed in region for guests.
     */
    public boolean prevent(Location location, Player player, String command) {
        if (prevent(location, player, Flag.command)) {
            return true;
        }
        if (accesseble(player)) {
            return false;
        }
        return preventCommands.contains(command.toLowerCase());
    }
    /**
     * Prevent guests use some items like bone meal.
     */
    public boolean prevent(Location location, Player player, ItemStack item) {
        return item != null && item.getType() == Material.INK_SACK && item.getDurability() == 15 && prevent(location, player, Flag.use);
    }
    /**
     * Check guest can use some material.
     */
    public boolean prevent(Location location, Player player, Material material) {
        if (preventUse.contains(material)) {
            return prevent(location, player, Flag.use);
        }
        return false;
    }
    /**
     * Check player is owner/member of any region inside location with true prevent flag.
     */
    public boolean prevent(Location location, Player player, Flag flag) {
        if (player.hasPermission(Permission.admin)) {
            return false;
        }
        Collection<RegionCube> regions = get(location);
        if (regions.size() > 0) {
            for (RegionCube region : regions) {
                if (region.contains(flag) && (!region.contains(player.getUniqueId()))) {
                    return true;
                }
            }
        }
        return WorldPrevent(location.getWorld(), flag);
    }
    /**
     * Search any region inside location with true prevent flag.
     */
    public boolean prevent(Location location, Flag flag) {
        Collection<RegionCube> regions = get(location);
        if (regions.size() > 0) {
            boolean prevent = false;
            for (RegionCube region : regions) {
                if (region.contains(flag)) {
                    prevent = true;
                    break;
                }
            }
            return prevent;
        }
        return WorldPrevent(location.getWorld(), flag);
    }
    /**
     * Check player member or owner of region.
     *
     * @param player Player
     */
    public boolean accesseble(Player player) {
        if (player.hasPermission(Permission.admin)) {
            return true;
        }
        Collection<RegionCube> regions = get(player.getLocation());
        if (regions.isEmpty()) {
            return true;
        }
        UUID uuid = player.getUniqueId();
        for (RegionCube region : regions) {
            if (region.contains(uuid)) {
                return true;
            }
        }
        return false;
    }

    private boolean WorldPrevent(World world, Flag flag) {
        return worldFlags.get(world.getName()).contains(flag);
    }

    public Collection<Flag> getWorldFlags(World world) {
        return worldFlags.get(world.getName());
    }

    public void setWorldFlag(String world, Flag flag, boolean value) {
        if (value) {
            worldFlags.get(world).add(flag);
        } else {
            worldFlags.get(world).remove(flag);
        }
    }
}
