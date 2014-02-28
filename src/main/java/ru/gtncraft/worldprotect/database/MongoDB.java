package ru.gtncraft.worldprotect.database;

import com.mongodb.*;
import org.bukkit.World;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.region.Region;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class MongoDB implements Storage {

    private final DB db;
    private final MongoClient client;

    public MongoDB(final WorldProtect plugin) throws IOException {
        this.client = new MongoClient(
            plugin.getConfig().getString("storage.host"),
            plugin.getConfig().getInt("storage.port")
        );
        this.db = client.getDB(plugin.getConfig().getString("storage.name"));
    }


    @Override
    public void save(final World world, final Collection<Region> regions) {
        final DBCollection coll = db.getCollection(world.getName());
        if (coll.count() == 0) {
            coll.ensureIndex(new BasicDBObject("name", 1).append("unique", true));
        }
        for (final Region region : regions) {
            region.update();
            coll.update(new BasicDBObject("name", region.getName()), region, true, false);
        }
    }

    @Override
    public void delete(final World world, final String name) {
        db.getCollection(world.getName()).remove(new BasicDBObject("name", name));
    }

    @Override
    public Collection<Region> load(final World world) {
        final Collection<Region> result = new ArrayList<>();
        final DBCollection coll = db.getCollection(world.getName());
        try (final DBCursor curr = coll.find(new BasicDBObject("name", new BasicDBObject("$exists", true)))) {
            while (curr.hasNext()) {
                result.add(new Region(curr.next().toMap(), world));
            }
        }
        return result;
    }

    @Override
    public void close() throws Exception {
        client.close();
    }
}
