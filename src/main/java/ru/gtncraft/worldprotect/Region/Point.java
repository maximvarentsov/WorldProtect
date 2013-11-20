package ru.gtncraft.worldprotect.Region;

import com.mongodb.BasicDBObject;
import org.bukkit.Location;
import java.util.Map;

final public class Point extends BasicDBObject {

    public Point(Location location) {
        this.put("x", Math.ceil(location.getX()));
        this.put("y", Math.ceil(location.getY()));
        this.put("z", Math.ceil(location.getZ()));
    }

    public Point(Map map) {
        this.putAll(map);
    }

    public double getX() {
        return getDouble("x");
    }

    public double getY() {
        return getDouble("y");
    }

    public double getZ() {
        return getDouble("z");
    }

    @Override
    public String toString() {
        return getX() + "," + getY() + "," + getZ();
    }
}
