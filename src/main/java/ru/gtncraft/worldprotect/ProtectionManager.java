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
    private final WorldProtect plugin;
    private final Map<String, Collection<RegionCube>> regions;
    private final Map<String, Collection<Flag>> worldFlags;
    private final Map<String, Table<Integer, Integer, Collection<RegionCube>>> chunks;

    private final Collection<String> worlds;
    private final Collection<String> preventCommands;
    private final Collection<Material> preventUse;

    public ProtectionManager(final WorldProtect plugin) {
        this.plugin = plugin;
        this.regions = new HashMap<>();
        this.worldFlags = new HashMap<>();
        this.chunks = new HashMap<>();
        this.storage = new Json(plugin);

        this.worlds = plugin.getConfig().getStringList("region.worlds");
        this.preventCommands = plugin.getConfig().getStringList("region.prevent.commands");

        this.preventUse = new ArrayList<>();
        for (String value : plugin.getConfig().getStringList("region.prevent.use")) {
            Material material = Material.matchMaterial(value.toUpperCase());
            if (material != null) {
                this.preventUse.add(material);
            }
        }
    }
    /**
     * Load regions for world.
     *
     * @param world World
     */
    public void load(final World world) throws IOException {
        Collection<Flag> flags = new ArrayList<>();
        Collection<RegionCube> values = new ArrayList<>();
        Table<Integer, Integer, Collection<RegionCube>> table = HashBasedTable.create();

        if (worlds.contains(world.getName())) {
            DataHolder data = storage.load(world);
            if (data != null) {
                for (RegionCube region : data.getRegions()) {
                    for (Map.Entry<Integer, Integer> entry : region.getChunks().entries()) {
                        int x = entry.getKey();
                        int z = entry.getValue();
                        if (!table.contains(x, z)) {
                            table.put(x, z, new ArrayList<RegionCube>());
                        }
                        table.get(x, z).add(region);
                    }
                }
                flags.addAll(data.getFlags());
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
    public void delete(final World world, final String name) {
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
    public void save(final World world) throws IOException {
        if (worlds.contains(world.getName())) {
            plugin.getLogger().info("Save region for world " + world.getName());
            storage.save(world, new DataHolder(get(world), getWorldFlags(world)));
        }
    }
    /**
     * Save and unload world regions.
     *
     * @param world World.
     */
    public void unload(final World world) {
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
    public void add(final World world, final RegionCube region) {
        regions.get(world.getName()).add(region);
        Table<Integer, Integer, Collection<RegionCube>> table = chunks.get(world.getName());
        for (Map.Entry<Integer, Integer> entry : region.getChunks().entries()) {
            int x = entry.getKey();
            int z = entry.getValue();
            if (!table.contains(x, z)) {
                table.put(x, z, new ArrayList<RegionCube>());
            }
            table.get(x, z).add(region);
        }
    }

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
    public Collection<RegionCube> get(final World world) {
        return regions.get(world.getName());
    }
    /**
     * Get regions inside current location.
     *
     * @param location Current location.
     */
    public Collection<RegionCube> get(final Location location) {
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
    public Collection<RegionCube> get(World world, RegionCube region) {
        Collection<RegionCube> result = new ArrayList<>();
        for (RegionCube overlay : get(world)) {
            if (overlay.contains(region) || region.contains(overlay)) {
                result.add(overlay);
            }
        }
        return result;
    }
    /**
     * If player is region guest and command not allowed in region for guests.
     */
    public boolean prevent(final Location location, final Player player, final String command) {
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
    public boolean prevent(final Location location, final Player player, final ItemStack item) {
        return item != null && item.getType() == Material.INK_SACK && item.getDurability() == 15 && prevent(location, player, Flag.use);
    }
    /**
     * Check guest can use some material.
     */
    public boolean prevent(final Location location, final Player player, final Material material) {
        if (preventUse.contains(material)) {
            return prevent(location, player, Flag.use);
        }
        return false;
    }
    /**
     * Check player is owner/member of any region inside location with true prevent flag.
     */
    public boolean prevent(final Location location, final Player player, final Flag flag) {
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
    public boolean prevent(final Location location, final Flag flag) {
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
    public boolean accesseble(final Player player) {

        if (player.hasPermission(Permission.admin)) {
            return true;
        }

        Collection<RegionCube> regions = get(player.getLocation());

        if (regions.isEmpty()) {
            return true;
        }

        UUID uuid = player.getUniqueId();
        for (RegionCube region : regions) {
            if (region.getOwners().contains(uuid) || region.getMembers().contains(uuid)) {
                return true;
            }
        }

        return false;
    }

    boolean WorldPrevent(final World world, final Flag flag) {
        return worldFlags.get(world.getName()).contains(flag);
    }

    public Collection<Flag> getWorldFlags(final World world) {
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
