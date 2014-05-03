package ru.gtncraft.worldprotect.database;

import com.google.common.collect.ImmutableList;
import org.bukkit.World;
import org.mongodb.*;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.region.Region;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Stream;

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
    public void save(final World world, final Stream<Region> regions) {
        MongoCollection<Document> collection = db.getCollection(world.getName());

        collection.tools().createIndexes(
            ImmutableList.of(Index.builder().addKey("name").unique().build())
        );

        regions.forEach(region -> {
            region.update();
            collection.find(new Document("name", region.getName())).upsert().updateOne(region);
        });
    }

    @Override
    public void delete(final World world, final String name) {
        db.getCollection(world.getName()).find(new Document("name", name)).removeOne();
    }

    @Override
    public Stream<Region> load(final World world) {
        Collection<Region> regions = new LinkedList<>();
        try (MongoCursor cursor = db.getCollection(world.getName()).find().get()) {
            cursor.forEachRemaining(obj -> regions.add(new Region((Document) obj, world)));
        }
        return regions.stream();
    }

    @Override
    public void close() throws IOException {
        client.close();
    }
}
