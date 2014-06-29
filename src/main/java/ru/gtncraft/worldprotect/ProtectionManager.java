package ru.gtncraft.worldprotect;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.gtncraft.worldprotect.storage.JsonFile;
import ru.gtncraft.worldprotect.storage.MongoDB;
import ru.gtncraft.worldprotect.storage.ProtectedWorld;
import ru.gtncraft.worldprotect.storage.Storage;
import ru.gtncraft.worldprotect.region.Flags;
import ru.gtncraft.worldprotect.region.Role;
import ru.gtncraft.worldprotect.flags.Prevent;
import ru.gtncraft.worldprotect.region.Cuboid;
import ru.gtncraft.worldprotect.region.Region;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProtectionManager {

    final Storage storage;
    final WorldProtect plugin;
    final Map<String, Collection<Region>> regions;
    final Map<String, Flags> worldFlags;
    final Map<String, Table<Integer, Integer, Collection<Region>>> chunks;
    final Collection<String> preventCommands;
    final Collection<Material> preventUse;

    public ProtectionManager(final WorldProtect plugin) throws IOException {
        this.plugin = plugin;
        this.regions = new HashMap<>();
        this.worldFlags = new HashMap<>();
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
        for (World world : Bukkit.getServer().getWorlds()) {
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
    public void load(final World world) throws IOException {
        Collection<Region> values = new ArrayList<>();
        Table<Integer, Integer, Collection<Region>> table = HashBasedTable.create();

        ProtectedWorld data = storage.load(world);

        if (plugin.getConfig().useRegions(world)) {
            data.getRegions().stream().forEach(region -> {
                region.getCuboid().getChunks().entries().stream().forEach(entry -> {
                    int x = entry.getKey();
                    int z = entry.getValue();
                    if (!table.contains(x, z)) {
                        table.put(x, z, new ArrayList<>());
                    }
                    table.get(x, z).add(region);
                });
                values.add(region);
            });
        }

        worldFlags.put(world.getName(), data.getFlags());
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
        get(world, name).ifPresent((region) -> {
            storage.delete(world, name);
            region.getCuboid().getChunks().entries().forEach((entry) -> {
                int x = entry.getKey();
                int z = entry.getValue();
                Collection<Region> coll = chunks.get(world.getName()).get(x, z);
                coll.remove(region);
                if (coll.isEmpty()) {
                    chunks.get(world.getName()).remove(x, z);
                }
            });
            regions.get(world.getName()).remove(region);
        });
    }
    /**
     * Save world regions.
     *
     * @param world World.
     */
    public void save(final World world) {
        if (plugin.getConfig().useRegions(world)) {
            plugin.getLogger().info("Save region for world " + world.getName());
            storage.save(world, new ProtectedWorld(
                    get(world).map(Region::update).collect(Collectors.toList()),
                    getWorldFlags(world)
            ));
        }
    }
    /**
     * Save all worlds regions and close storage.
     */
    public void disable() {
        Bukkit.getWorlds().forEach(this::save);
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
        worldFlags.remove(world.getName());
    }
    /**
     * Add new region in world.
     *
     * @param world World.
     * @param region region.
     */
    public void add(final World world, final Region region) {
        regions.get(world.getName()).add(region);
        Table<Integer, Integer, Collection<Region>> table = chunks.get(world.getName());
        region.getCuboid().getChunks().entries().stream().forEach(e -> {
            int x = e.getKey();
            int z = e.getValue();
            if (!table.contains(x, z)) {
                table.put(x, z, new ArrayList<>());
            }
            table.get(x, z).add(region);
        });
    }
    /**
     * Get world regions.
     *
     * @param world World.
     */
    public Stream<Region> get(final World world) {
        return regions.get(world.getName()).stream();
    }
    /**
     * Get regions inside current location.
     *
     * @param location Current location.
     */
    public Optional<Collection<Region>> get(final Location location) {
        Chunk chunk = location.getChunk();
        Collection<Region> regions = chunks.get(location.getWorld().getName()).get(chunk.getX(), chunk.getZ());
        if (regions == null) {
            return Optional.empty();
        }
        Collection<Region> result = new ArrayList<>();
        for (Region region : regions) {
            if (region.contains(location)) {
                result.add(region);
            }
        }
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    /**
     * Get region in current world by name.
     *
     * @param world World.
     * @param name region name.
     */
    public Optional<Region> get(final World world, final String name) {
        return get(world).filter(regions -> regions.getName().equalsIgnoreCase(name)).findAny();
    }
    /**
     * Return regions owned by player in current world.
     *
     * @param player Player.
     * @param role Player Roles in region.
     */
    public Stream<Region> get(final Player player, final Role role) {
        return get(player.getWorld()).filter(region -> region.player(player, role));
    }
    /**
     * Get regions overlays.
     *
     * @param value region.
     */
    public Stream<Region> get(final Region value) {
        Cuboid cuboid = value.getCuboid();
        return get(cuboid.getWorld()).filter(region ->
            region.contains(cuboid.getLowerNE()) || region.contains(cuboid.getUpperSW()) ||
            cuboid.contains(region.getCuboid().getLowerNE()) || cuboid.contains(region.getCuboid().getUpperSW())
        );
    }
    /**
     * If player is region guest and command not allowed in region for guests.
     */
    public boolean prevent(final Location location, final Player player, final String command) {
        if (prevent(location, player, Prevent.command)) {
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
        return item != null && item.getType() == Material.INK_SACK && item.getDurability() == 15 && prevent(location, player, Prevent.use);
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
        Optional<Collection<Region>> regions = get(location);
        if (regions.isPresent()) {
            for (Region region : regions.get()) {
                if (region.flag(flag) && !region.player(player)) {
                    return true;
                }
            }
        }
        return WorldPrevent(location.getWorld(), flag);
    }
    /**
     * Search any region inside location with true prevent flag.
     */
    public boolean prevent(final Location location, final Prevent flag) {
        Optional<Collection<Region>> regions = get(location);
        if (regions.isPresent()) {
            boolean prevent = false;
            for (Region region : regions.get()) {
                if (region.flag(flag)) {
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

        if (player.hasPermission(Permissions.admin) || player.hasPermission(Permissions.moder)) {
            return true;
        }

        Optional<Collection<Region>> regions = get(player.getLocation());

        if (!regions.isPresent()) {
            return true;
        }

        for (Region region : regions.get()) {
            if (region.player(player)) {
                return true;
            }
        }

        return false;
    }

    boolean WorldPrevent(final World world, final Prevent flag) {
        return worldFlags.get(world.getName()).get(flag);
    }

    public Flags getWorldFlags(final World world) {
        return worldFlags.get(world.getName());
    }

    public void setWorldFlag(final String world, final Prevent flag, final boolean value) {
        worldFlags.get(world).set(flag, value);
    }
}
