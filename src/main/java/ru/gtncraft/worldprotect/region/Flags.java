package ru.gtncraft.worldprotect.region;

import com.google.common.collect.ImmutableMap;
import ru.gtncraft.worldprotect.Entity;
import ru.gtncraft.worldprotect.flags.Prevent;

import java.util.Map;

public class Flags extends Entity {

    public Flags(final Map map) {
        super(map);
    }

    public Flags() {
        this(ImmutableMap.of());
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
