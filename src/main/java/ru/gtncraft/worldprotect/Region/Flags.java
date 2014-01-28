package ru.gtncraft.worldprotect.Region;

import com.mongodb.BasicDBObject;
import java.util.Map;

public class Flags extends BasicDBObject {
    public static enum prevent {
        build,
        use,
        damage,
        teleport,
        creatureSpawn,
        command,
        explode,
        leavesDecay,
        grow,
        fade,
        burn
    }
    public Flags(final Map map) {
        putAll(map);
    }
    /**
     * Set default flags state.
     */
    public Flags() {
        set(prevent.build, true);
        set(prevent.use, true);
    }
    /**
     * Change flag state.
     */
    public void set(final prevent flag, boolean value) {
        put(flag.name(), value);
    }
    /**
     * Get flag state.
     */
    public boolean get(final prevent flag) {
        return getBoolean(flag.name(), false);
    }
}
