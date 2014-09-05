package ru.gtncraft.worldprotect.region;

import java.util.ArrayList;
import java.util.Collection;

public enum Flag {
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
    bukkitEmptyLava,
    portalCreation;

    final static Collection<String> values = new ArrayList<>();

    static {
        for (Flag flag : values()) {
            values.add(flag.name());
        }
    }

    public static Collection<String> toArray() {
        return values;
    }
}
