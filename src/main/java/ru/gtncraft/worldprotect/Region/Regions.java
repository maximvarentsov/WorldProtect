package ru.gtncraft.worldprotect.Region;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

final public class Regions extends ConcurrentHashMap<String, Region> {
    /**
     * Return all regions in location
     */
    public List<Region> get(Location location) {
        List<Region> result = new ArrayList<>();
        for (Map.Entry<String, Region> entry : entrySet()) {
            if (entry.getValue().contains(location)) {
                result.add(entry.getValue());
            }
        }
        return result;
    }
    /**
     * Return region by name.
     */
    public Region get(String name) {
        for (Map.Entry<String, Region> entry : entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }
    /**
     * Return regions owned by player.
     */
    public List<Region> getOwned(Player player) {
        ArrayList<Region> result = new ArrayList<>();
        for (Map.Entry<String, Region> entry : entrySet()) {
            if (entry.getValue().isOwner(player)) {
                result.add(entry.getValue());
            }
        }
        return result;
    }
    /**
     * Add new region.
     */
    public void add(Region region) {
        this.put(region.getName(), region);
    }
}
