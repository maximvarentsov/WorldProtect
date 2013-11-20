package ru.gtncraft.worldprotect;

import com.mongodb.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import ru.gtncraft.worldprotect.Region.Region;
import ru.gtncraft.worldprotect.Region.Regions;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

final public class RegionManager extends ConcurrentHashMap<String, Regions> {

    final private DB db;

    public RegionManager(final ConfigurationSection config) throws IOException {
        MongoClient mongoClient = new MongoClient(config.getString("host", "localhost"), config.getInt("port", 27017));
        this.db = mongoClient.getDB(config.getString("name", "worldprotect"));
    }

    public void load(String world) {
        DBCollection coll = db.getCollection(world);
        if (coll.count() < 1) {
            coll.ensureIndex(new BasicDBObject("name", 1));
        }
        Regions regions = new Regions();
        try (DBCursor curr = coll.find()) {
            while (curr.hasNext()) {
                regions.add(new Region(curr.next().toMap()));
            }
        }
        this.put(world, regions);
    }

    public void unload(String world) {
        save(world);
        this.remove(world);
    }

    public void delete(final World world, String name) {
        getRegions(world).remove(name);
        DBCollection coll = db.getCollection(world.getName());
        coll.remove(new BasicDBObject("name", name));
    }

    public void save(String world) {
        DBCollection coll = db.getCollection(world);
        for (Map.Entry<String, Region> entry : this.get(world).entrySet()) {
            DBObject query = new BasicDBObject("name", entry.getKey());
            coll.findAndModify(query, null, null, false, entry.getValue(), false, true);
        }
    }

    public Regions getRegions(final World world) {
        return this.get(world.getName());
    }

    public List<Region> getRegions(final Location location) {
        return this.get(location.getWorld().getName()).get(location);
    }

    @Override
    public Set<Entry<String, Regions>> entrySet() {
        return super.entrySet();
    }
}
