package ru.gtncraft.worldprotect.database;

import com.mongodb.*;
import org.bukkit.World;
import ru.gtncraft.worldprotect.Region.Region;
import ru.gtncraft.worldprotect.WorldProtect;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MongoDB implements Storage {

    private final DB db;

    public MongoDB(final WorldProtect plugin) throws IOException {
        MongoClient mongoClient = new MongoClient(
                plugin.getConfig().getString("storage.host", "localhost"),
                plugin.getConfig().getInt("storage.port", 27017)
        );
        db = mongoClient.getDB(plugin.getConfig().getString("storage.name", "worldprotect"));
    }


    @Override
    public void save(World world, Map<String, Region> regions) {
        DBCollection coll = db.getCollection(world.getName());
        for (Map.Entry<String, Region> entry : regions.entrySet()) {
            Region region = entry.getValue();
            region.update();
            DBObject query = new BasicDBObject("name", region.getName());
            coll.update(query, region, true, false);
        }
    }

    @Override
    public void delete(World world, String name) {
        db.getCollection(world.getName()).remove(new BasicDBObject("name", name));
    }

    @Override
    public Map<String, Region> load(World world) {
        Map<String, Region> values = new HashMap<>();
        DBCollection coll = db.getCollection(world.getName());
        if (coll.count() == 0) {
            coll.ensureIndex(new BasicDBObject("name", 1).append("unique", true));
        }
        try (DBCursor curr = coll.find()) {
            while (curr.hasNext()) {
                Region region = new Region(curr.next().toMap());
                values.put(region.getName(), region);
            }
        }
        return values;
    }
}
