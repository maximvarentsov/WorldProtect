package ru.gtncraft.worldprotect.Region;

import com.mongodb.BasicDBList;
import java.util.Map;

final public class Players extends BasicDBList {

    public Players() {}

    public Players(Map map) {
        putAll(map);
    }

    public synchronized void _add(String player) {
        super.add(player.toLowerCase());
    }

    public synchronized void _remove(String player) {
        super.remove(player.toLowerCase());
    }

    public boolean has(String player) {
        return contains(player.toLowerCase());
    }
}
