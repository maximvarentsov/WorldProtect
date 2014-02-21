package ru.gtncraft.worldprotect.database;

import com.mongodb.*;
import org.bukkit.World;
import ru.gtncraft.worldprotect.Config;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.region.Region;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MongoDB implements Storage {

    private final DB db;
    private final Config config;
    private final MongoClient client;

    public MongoDB(final WorldProtect plugin) throws IOException {
        this.config = plugin.getConfig();
        this.client = new MongoClient(
            plugin.getConfig().getString("storage.host"),
            plugin.getConfig().getInt("storage.port")
        );
        this.db = client.getDB(plugin.getConfig().getString("storage.name"));
    }


    @Override
    public void save(final World world, final Map<String, Region> regions) {
        DBCollection coll = db.getCollection(world.getName());
        if (coll.count() == 0) {
            coll.ensureIndex(new BasicDBObject("name", 1).append("unique", true));
        }
        for (Region region : regions.values()) {
            region.update();
            DBObject query = new BasicDBObject("name", region.getName());
            coll.update(query, region, true, false);
        }
    }

    @Override
    public void delete(final World world, final String name) {
        db.getCollection(world.getName()).remove(new BasicDBObject("name", name));
    }

    @Override
    public Map<String, Region> load(final World world) {
        final Map<String, Region> values = new HashMap<>();
        if (config.useRegions(world)) {
            DBCollection coll = db.getCollection(world.getName());
            try (DBCursor curr = coll.find(new BasicDBObject("name", new BasicDBObject("$exists", true)))) {
                while (curr.hasNext()) {
                    Region region = new Region(curr.next().toMap(), world);
                    values.put(region.getName(), region);
                }
            }
        }
        return values;
    }

    @Override
    public void close() throws Exception {
        client.close();
    }
}
