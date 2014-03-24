package ru.gtncraft.worldprotect.flags;

import java.util.ArrayList;
import java.util.Collection;

public enum Prevent {
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
    pvp,
    piston,
    vehicleNaturalDestroy,
    fallingBlocks;

    public final static Collection<String> values = new ArrayList<>();

    static {
        for (final Prevent flag : values()) {
            values.add(flag.name());
        }
    }

    public static Collection<String> toArray() {
        return values;
    }
}
