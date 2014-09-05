package ru.gtncraft.worldprotect.region;

import java.util.ArrayList;
import java.util.Collection;

public class DataHolder {
    private final Collection<Flag> flags = new ArrayList<>();
    private final Collection<RegionCube> regions = new ArrayList<>();

    public DataHolder(Collection<RegionCube> regions, Collection<Flag> flags) {
        this.regions.addAll(regions);
        this.flags.addAll(flags);
    }

    public Collection<RegionCube> getRegions() {
        return regions;
    }

    public Collection<Flag> getFlags() {
        return flags;
    }
}
