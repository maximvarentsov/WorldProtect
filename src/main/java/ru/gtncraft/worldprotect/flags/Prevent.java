package ru.gtncraft.worldprotect.flags;

import java.util.ArrayList;
import java.util.Collection;

public enum Prevent {
    build,
    use,
    damage,
    teleport,
    creatureSpawn,
    entityBlockExplode,
    command,
    explode,
    leavesDecay,
    grow,
    fade,
    burn,
    fireSpread,
    pvp,
    piston,
    vehicleNaturalDestroy,
    fallingBlocks,
    bukkitEmptyWater,
    bukkitEmptyLava;

    public final static Collection<String> values = new ArrayList<>();

    static {
        for (Prevent flag : values()) {
            values.add(flag.name());
        }
    }

    public static Collection<String> toArray() {
        return values;
    }
}
