package ru.gtncraft.worldprotect.Region;

import com.mongodb.BasicDBObject;
import ru.gtncraft.worldprotect.flags.Prevent;
import java.util.Map;

public class Flags extends BasicDBObject {

    public Flags(final Map map) {
        putAll(map);
    }
    /**
     * Set default flags state.
     */
    public Flags() {
        set(Prevent.build, true);
        set(Prevent.use, true);
    }
    /**
     * Change flag state.
     */
    public void set(final Prevent flag, boolean value) {
        put(flag.name(), value);
    }
    /**
     * Get flag state.
     */
    public boolean get(final Prevent flag) {
        return getBoolean(flag.name(), false);
    }
}
