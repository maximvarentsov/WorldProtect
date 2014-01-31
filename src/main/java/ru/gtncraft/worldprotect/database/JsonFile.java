package ru.gtncraft.worldprotect.database;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.bukkit.World;
import ru.gtncraft.worldprotect.Region.Region;
import ru.gtncraft.worldprotect.WorldProtect;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class JsonFile implements Storage {

    private final WorldProtect plugin;

    public JsonFile(final WorldProtect plugin) {
        this.plugin = plugin;
    }

    @Override
    public void save(World world, Map<String, Region> regions) {
        File file = getFile(world);
        try (OutputStream os = new FileOutputStream(file)) {
            BasicDBList list = new BasicDBList();
            for (Map.Entry<String, Region> entry : regions.entrySet()) {
                list.add(entry.getValue());
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

    public File getFile(World world) {
        return new File(plugin.getDataFolder().getAbsolutePath() + File.separator + world.getName() + ".json");
    }

    @Override
    public void delete(World world, String name) {}

    @Override
    public Map<String, Region> load(World world) {
        Map<String, Region> result = new HashMap<>();
        try (InputStream in = new FileInputStream(getFile(world))) {
            String json = CharStreams.toString(new InputStreamReader(in, Charsets.UTF_8));
            BasicDBList list = (BasicDBList) JSON.parse(json);
            if (list == null) {
                return result;
            }
            for (Object obj : list) {
                Region region = new Region(((DBObject) obj).toMap());
                result.put(region.getId(), region);
            }
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            plugin.getLogger().severe(ex.getMessage());
        }
        return result;
    }
}
