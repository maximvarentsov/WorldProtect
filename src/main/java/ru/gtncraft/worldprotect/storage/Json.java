package ru.gtncraft.worldprotect.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.World;
import ru.gtncraft.worldprotect.WorldProtect;
import ru.gtncraft.worldprotect.region.DataHolder;

import java.io.*;

public class Json implements Storage {
    private final Gson gson;
    private final File dataFolder;

    public Json(WorldProtect plugin) {
        gson = new GsonBuilder().setPrettyPrinting().create();
        dataFolder = plugin.getDataFolder();
    }

    private File getFile(World world) {
        return new File(dataFolder, world.getName() + ".json");
    }

    @Override
    public DataHolder load(World world) throws IOException {
        File file = getFile(world);
        if (!file.exists()) {
            return null;
        }
        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, DataHolder.class);
        }
    }

    @Override
    public void save(World world, DataHolder protectedWorld) throws IOException {
        String data = gson.toJson(protectedWorld);
        try (OutputStream os = new FileOutputStream(getFile(world))) {
            os.write(data.getBytes());
        }
    }

    @Override
    public boolean remove(World world) {
        return getFile(world).delete();
    }
}
