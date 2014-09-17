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
    grow,
    burn,
    spread,
    pvp,
    piston,
    vehicleNaturalDestroy,
    bukkitEmptyWater,
    bukkitEmptyLava,
    portalCreation,
    hungry;

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
