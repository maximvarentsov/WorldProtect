package ru.gtncraft.worldprotect.Region;

import com.mongodb.BasicDBObject;
import java.util.Map;

final public class Flags extends BasicDBObject {
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
        burn,
        snowmanGrief
    }
    public Flags(Map map) {
        putAll(map);
    }
    /**
     * Set default flags state.
     */
    public Flags() {
        put(prevent.build.name(), true);
        put(prevent.use.name(), true);
    }
    /**
     * Change flag state.
     */
    public void set(prevent flag, boolean value) {
        put(flag.name(), value);
    }
    /**
     * Get flage state.
     */
    public boolean get(prevent flag) {
        return getBoolean(flag.name(), false);
    }
}
