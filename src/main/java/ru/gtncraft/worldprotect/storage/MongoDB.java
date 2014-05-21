package ru.gtncraft.worldprotect.storage;

import com.google.common.collect.ImmutableList;
import org.bukkit.World;
import org.mongodb.*;
import ru.gtncraft.worldprotect.Config;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.region.Flags;
import ru.gtncraft.worldprotect.region.Region;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

public class MongoDB implements Storage {

    final MongoDatabase db;
    final MongoClient client;

    public MongoDB(final WorldProtect plugin) throws IOException {
        client = MongoClients.create(
                plugin.getConfig().getReplicaSet()
        );
        db = client.getDatabase(plugin.getConfig().getString("storage.name"));
    }

    @Override
    public void save(final World world, final ProtectedWorld data) {
        MongoCollection<Document> collection = db.getCollection(world.getName());

        collection.tools().createIndexes(
            ImmutableList.of(Index.builder().addKey("name").unique().build())
        );

        data.getRegions().forEach(region -> {
            region.update();
            collection.find(new Document("name", region.getName())).upsert().updateOne(region);
        });

        collection.find(new Document("world", world.getName())).upsert().updateOne(data.getFlags());
    }

    @Override
    public void delete(final World world, final String name) {
        db.getCollection(world.getName()).find(new Document("name", name)).removeOne();
    }

    @Override
    public ProtectedWorld load(final World world) {
        Collection<Region> regions = new LinkedList<>();

        Flags worldFlags = new Flags(Config.getInstance().getWorldFlags());

        try (MongoCursor cursor = db.getCollection(world.getName()).find().get()) {
            cursor.forEachRemaining(obj -> regions.add(new Region((Document) obj, world)));
            Document document = db.getCollection("world_flags").find(new Document("world", world.getName())).getOne();
            if (document != null) {
                worldFlags = new Flags(worldFlags);
            }
        }

        return new ProtectedWorld(regions, worldFlags);
    }

    @Override
    public void close() throws IOException {
        client.close();
    }
}
