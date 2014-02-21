package ru.gtncraft.worldprotect.database;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.bukkit.World;
import ru.gtncraft.worldprotect.Config;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.region.Region;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class JsonFile implements Storage {

    private final WorldProtect plugin;
    private final Config config;

    public JsonFile(final WorldProtect plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public void save(final World world, final Map<String, Region> regions) {
        final File file = getFile(world);
        try (OutputStream os = new FileOutputStream(file)) {
            BasicDBList list = new BasicDBList();
            for (Region region : regions.values()) {
                region.update();
                list.add(region);
            }
            if (list.size() > 0) {
                if (!file.exists()) {
                    file.createNewFile();
                }
                os.write(list.toString().getBytes());
            }
        } catch (IOException ex) {
            plugin.getLogger().severe(ex.getMessage());
        }
    }

    public File getFile(final World world) {
        return new File(plugin.getDataFolder().getAbsolutePath() + File.separator + world.getName() + ".json");
    }

    @Override
    public void delete(final World world, final String name) {
    }

    @Override
    public Map<String, Region> load(final World world) {
        final Map<String, Region> result = new HashMap<>();
        if (config.useRegions(world)) {
            try (InputStream in = new FileInputStream(getFile(world))) {
                String json = CharStreams.toString(new InputStreamReader(in, Charsets.UTF_8));
                BasicDBList list = (BasicDBList) JSON.parse(json);
                if (list == null) {
                    return result;
                }
                for (Object obj : list) {
                    Region region = new Region(((DBObject) obj).toMap(), world);
                    result.put(region.getName(), region);
                }
            } catch (FileNotFoundException ex) {
            } catch (IOException ex) {
                plugin.getLogger().severe(ex.getMessage());
            }
        }
        return result;
    }

    @Override
    public void close() throws Exception {

    }
}
