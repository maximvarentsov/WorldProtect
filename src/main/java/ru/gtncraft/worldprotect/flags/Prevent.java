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

    public static Collection<String> toArray() {
        final Collection<String> result = new ArrayList<>();
        for (final Prevent flag : values()) {
            result.add(flag.name());
        }
        return result;
    }
}
