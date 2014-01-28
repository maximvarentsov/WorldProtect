package ru.gtncraft.worldprotect.Region;

import com.mongodb.BasicDBObject;
import org.bukkit.Location;
import java.util.Map;

public class Point extends BasicDBObject {

    public Point(final Location location) {
        this.put("x", location.getBlockX());
        this.put("y", location.getBlockY());
        this.put("z", location.getBlockZ());
    }

    public Point(final Map map) {
        this.putAll(map);
    }

    public int getX() {
        return getInt("x");
    }

    public int getY() {
        return getInt("y");
    }

    public int getZ() {
        return getInt("z");
    }

    @Override
    public String toString() {
        return getX() + "," + getY() + "," + getZ();
    }
}
