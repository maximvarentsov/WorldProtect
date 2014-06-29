package ru.gtncraft.worldprotect.storage;

import com.cedarsoftware.util.io.JsonWriter;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import org.bson.codecs.DecoderContext;
import org.bson.json.JsonReader;
import org.bukkit.World;
import org.mongodb.Document;
import org.mongodb.codecs.DocumentCodec;
import ru.gtncraft.worldprotect.Entity;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.region.Flags;
import ru.gtncraft.worldprotect.region.Region;

import java.io.*;
import java.util.*;

public class JsonFile implements Storage {

    final WorldProtect plugin;

    public JsonFile(final WorldProtect plugin) {
        this.plugin = plugin;
    }

    @Override
    public void save(final World world, final ProtectedWorld data) {
        try {
            List<Document> docs = new ArrayList<>();
            for (Region region : data.getRegions()) {
                docs.add(region.toDocument());
            }
            Document entity = new Document(ImmutableMap.of(
                    "world", new Document(ImmutableMap.of(
                            "flags", data.getFlags().toDocument()
                    )),
                    "regions", docs
            ));
            byte[] bytes = JsonWriter.formatJson(entity.toString()).getBytes();
            if (!entity.isEmpty()) {
                try (OutputStream os = new FileOutputStream(getFile(world))) {
                    os.write(bytes);
                } catch(IOException ex) {
                    plugin.getLogger().severe(ex.getMessage());
                }
            }
        } catch (IOException ex) {
            plugin.getLogger().severe(ex.getMessage());
        }
    }

    File getFile(final World world) {
        return new File(plugin.getDataFolder().getAbsolutePath() + File.separator + world.getName() + ".json");
    }

    Entity parse(final String data) {
        return new Entity(new DocumentCodec().decode(new JsonReader(data), DecoderContext.builder().build()));
    }

    @Override
    public void delete(final World world, final String name) {}

    @Override
    public ProtectedWorld load(final World world) {
        Collection<Region> regions = new LinkedList<>();
        Entity worldFlags = new Flags(plugin.getConfig().getWorldFlags());

        try (InputStream is = new FileInputStream(getFile(world))) {
            String json = CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
            Entity document = parse(json);
            worldFlags = new Flags(document.asEntity("world").asEntity("flags"));
            document.<Map<String, Object>>stream("regions").map(map -> new Region(map, world)).forEach(regions::add);

        } catch (FileNotFoundException ignore) {
        } catch (IOException ex) {
            plugin.getLogger().severe(ex.getMessage());
        }

        return new ProtectedWorld(regions, worldFlags);
    }

    @Override
    public void close() throws IOException {}
}
