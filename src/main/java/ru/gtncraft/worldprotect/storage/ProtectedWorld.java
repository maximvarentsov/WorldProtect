package ru.gtncraft.worldprotect.storage;

import ru.gtncraft.worldprotect.flags.Prevent;
import ru.gtncraft.worldprotect.region.Flags;
import ru.gtncraft.worldprotect.region.Region;

import java.util.Collection;
import java.util.Map;

public class ProtectedWorld {

    final Collection<Region> regions;
    final Map<String, Object> flags;

    public ProtectedWorld(Collection<Region> regions, Map<String, Object> flags) {
        this.regions = regions;
        this.flags = flags;
    }

    public Collection<Region> getRegions() {
        return regions;
    }

    public Flags getFlags() {
        Flags result = new Flags();
        Prevent flag;
        boolean value;
        for (Map.Entry<String, Object> entry : flags.entrySet()) {
            try {
                flag = Prevent.valueOf(entry.getKey());
                value = (boolean) entry.getValue();
                result.set(flag, value);
            } catch (Throwable ignored) {
            }
        }
        return result;
    }
}
