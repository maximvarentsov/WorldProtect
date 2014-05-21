package ru.gtncraft.worldprotect.database;

import com.cedarsoftware.util.io.JsonWriter;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.bson.BSONReader;
import org.bukkit.World;
import org.mongodb.Document;
import org.mongodb.codecs.DocumentCodec;
import org.mongodb.json.JSONMode;
import org.mongodb.json.JSONReader;
import org.mongodb.json.JSONReaderSettings;
import ru.gtncraft.worldprotect.Entity;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.region.Region;

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Stream;

public class JsonFile implements Storage {

    final WorldProtect plugin;

    public JsonFile(final WorldProtect plugin) {
        this.plugin = plugin;
    }

    @Override
    public void save(final World world, final Stream<Region> regions) {
        Collection<Region> values = new LinkedList<>();
        regions.map(Region::update).forEach(values::add);
        try {
            byte[] bytes = JsonWriter.formatJson(new Document("regions", values).toString()).getBytes();
            if (!values.isEmpty()) {
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

    public File getFile(final World world) {
        return new File(plugin.getDataFolder().getAbsolutePath() + File.separator + world.getName() + ".json");
    }

    public Entity parse(final String data) throws Exception {
        BSONReader bsonReader = new JSONReader(new JSONReaderSettings(JSONMode.STRICT), data);
        return new Entity(new DocumentCodec().decode(bsonReader));
    }

    @Override
    public void delete(final World world, final String name) {
    }

    @Override
    public Stream<Region> load(final World world) {
        try (InputStream in = new FileInputStream(getFile(world))) {
            String json = CharStreams.toString(new InputStreamReader(in, Charsets.UTF_8));
            try {
                return parse(json).<Map>stream("regions").map(e -> new Region(e, world));
            } catch (Throwable ex) {
                plugin.getLogger().warning("Could not parse " + world.getName() + ".json.");
            }
        } catch (FileNotFoundException ignore) {
        } catch (IOException ex) {
            plugin.getLogger().severe(ex.getMessage());
        }
        return Stream.of();
    }

    @Override
    public void close() throws IOException {
    }
}
