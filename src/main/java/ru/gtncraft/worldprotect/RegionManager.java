package ru.gtncraft.worldprotect;

import com.mongodb.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import ru.gtncraft.worldprotect.Region.Players;
import ru.gtncraft.worldprotect.Region.Region;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final public class RegionManager {

    final private DB db;
    final private Map<String, Map<String, Region>> regions;

    public RegionManager(final ConfigurationSection config) throws IOException {
        MongoClient mongoClient = new MongoClient(config.getString("host", "localhost"), config.getInt("port", 27017));
        this.db = mongoClient.getDB(config.getString("name", "worldprotect"));
        this.regions = new HashMap<>();
    }
    /**
     * Load regions for world.
     *
     * @param world World
     */
    public void load(final World world) {
        Map<String, Region> values = new HashMap<>();
        // Init collection.
        DBCollection coll = db.getCollection(world.getName());
        if (coll.count() < 1) {
            coll.ensureIndex(new BasicDBObject("name", 1));
        }
        // Get regions.
        try (DBCursor curr = coll.find()) {
            while (curr.hasNext()) {
                Region region = new Region(curr.next().toMap());
                values.put(region.getName(), region);
            }
        }
        regions.put(world.getName(), values);
    }
    /**
     * Delete region from world.
     *
     * @param world World
     * @param name Region name
     */
    public void delete(final World world, final String name) {
        get(world).remove(name);
        DBCollection coll = db.getCollection(world.getName());
        coll.remove(new BasicDBObject("name", name));
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
     * Save world regions.
     *
     * @param world World.
     */
    public void save(final World world) {
        DBCollection coll = db.getCollection(world.getName());
        for (Map.Entry<String, Region> entry : get(world).entrySet() ) {
            DBObject query = new BasicDBObject("name", entry.getKey());
            coll.findAndModify(query, null, null, false, entry.getValue(), false, true);
        }
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
     * Get regions inside current location.
     *
     * @param location Current location.
     */
    public List<Region> get(final Location location) {
        List<Region> result = new ArrayList<>();
        for (Map.Entry<String, Region> entry : get(location.getWorld()).entrySet()) {
            if (entry.getValue().contains(location)) {
                result.add(entry.getValue());
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
     * @param role Player role in region.
     */
    public List<Region> get(final Player player, final Players.role role) {
        ArrayList<Region> result = new ArrayList<>();
        for (Map.Entry<String, Region> entry : get(player.getWorld()).entrySet()) {
            Region region = entry.getValue();
            if (region.is(player, role)) {
                result.add(region);
            }
        }
        return result;
    }
}
