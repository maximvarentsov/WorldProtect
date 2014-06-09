package ru.gtncraft.worldprotect.storage;

import com.cedarsoftware.util.io.JsonWriter;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import org.bson.BSONReader;
import org.bukkit.World;
import org.mongodb.codecs.DocumentCodec;
import org.mongodb.json.JSONMode;
import org.mongodb.json.JSONReader;
import org.mongodb.json.JSONReaderSettings;
import ru.gtncraft.worldprotect.Entity;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.region.Flags;
import ru.gtncraft.worldprotect.region.Region;

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

public class JsonFile implements Storage {

    final WorldProtect plugin;

    public JsonFile(final WorldProtect plugin) {
        this.plugin = plugin;
    }

    @Override
    public void save(final World world, final ProtectedWorld data) {
        try {
            Entity entity = new Entity(ImmutableMap.of(
                    "world", new Entity(ImmutableMap.of("flags", data.getFlags())),
                    "regions", data.getRegions())
            );
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
        BSONReader bsonReader = new JSONReader(new JSONReaderSettings(JSONMode.STRICT), data);
        return new Entity(new DocumentCodec().decode(bsonReader));
    }

    @Override
    public void delete(final World world, final String name) {}

    @Override
    public ProtectedWorld load(final World world) {
        Collection<Region> regions = new LinkedList<>();
        Entity worldFlags = new Flags(plugin.getConfig().getWorldFlags());

        try (InputStream is = new FileInputStream(getFile(world))) {
            String json = CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
            try {
                Entity document = parse(json);
                worldFlags = new Flags(document.asEntity("world").asEntity("flags"));
                document.<Map<String, Object>>stream("regions").map(e -> new Region(e, world)).forEach(regions::add);
            } catch (Throwable ex) {
                plugin.getLogger().warning("Could not parse " + world.getName() + ".json.");
            }
        } catch (FileNotFoundException ignore) {
        } catch (IOException ex) {
            plugin.getLogger().severe(ex.getMessage());
        }

        return new ProtectedWorld(regions, worldFlags);
    }

    @Override
    public void close() throws IOException {}
}
