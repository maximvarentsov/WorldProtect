package ru.gtncraft.worldprotect.database;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.bukkit.World;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.region.Region;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

public class JsonFile implements Storage {

    private final WorldProtect plugin;

    public JsonFile(final WorldProtect plugin) {
        this.plugin = plugin;
    }

    @Override
    public void save(final World world, final Collection<Region> regions) {
        final File file = getFile(world);
        try (final OutputStream os = new FileOutputStream(file)) {
            final BasicDBList list = new BasicDBList();
            for (final Region region : regions) {
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
    public Collection<Region> load(final World world) {
        final Collection<Region> result = new ArrayList<>();
        try (final InputStream in = new FileInputStream(getFile(world))) {
            final String json = CharStreams.toString(new InputStreamReader(in, Charsets.UTF_8));
            final BasicDBList list = (BasicDBList) JSON.parse(json);
            if (list == null) {
                return result;
            }
            for (final Object obj : list) {
                result.add(new Region(((DBObject) obj).toMap(), world));
            }
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            plugin.getLogger().severe(ex.getMessage());
        }
        return result;
    }

    @Override
    public void close() throws Exception {
    }
}
