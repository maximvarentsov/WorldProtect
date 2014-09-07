package ru.gtncraft.worldprotect.region;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class RegionCube {
    private final int lowerX, lowerY, lowerZ;
    private final int upperX, upperY, upperZ;
    private String name;
    private Collection<UUID> owners = new ArrayList<>();
    private Collection<UUID> members = new ArrayList<>();
    private Collection<Flag> flags = new ArrayList<>();

    public RegionCube(Location l1, Location l2) {
        this(l1.getBlockX(), l1.getBlockY(), l1.getBlockZ(), l2.getBlockX(), l2.getBlockY(), l2.getBlockZ());
    }

    public RegionCube(int x1, int y1, int z1, int x2, int y2, int z2) {
        lowerX = Math.min(x1, x2);
        lowerY = Math.min(y1, y2);
        lowerZ = Math.min(z1, z2);
        upperX = Math.max(x1, x2);
        upperY = Math.max(y1, y2);
        upperZ = Math.max(z1, z2);
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value.toLowerCase();
    }

    public void addFlag(Flag flag) {
        flags.add(flag);
    }

    public boolean addOwner(UUID uuid) {
        return owners.add(uuid);
    }

    public boolean addMember(UUID uuid) {
        return members.add(uuid);
    }

    public Collection<Flag> getFlags() {
        return flags;
    }

    public Collection<UUID> getOwners() {
        return owners;
    }

    public Collection<UUID> getMembers() {
        return members;
    }

    public void removeFlag(Flag flag) {
        flags.remove(flag);
    }

    public boolean removeOwner(UUID uuid) {
        return owners.remove(uuid);
    }

    public boolean removeMember(UUID uuid) {
        return members.remove(uuid);
    }

    public long volume() {
        int sizeX = (upperX - lowerX) + 1;
        int sizeY = (upperY - lowerY) + 1;
        int sizeZ = (upperZ - lowerZ) + 1;
        return sizeX * sizeY * sizeZ;
    }

    public Multimap<Integer, Integer> getChunks() {
        Multimap<Integer, Integer> result = ArrayListMultimap.create();
        int x1 = lowerX & ~0xf;
        int x2 = upperX & ~0xf;
        int z1 = lowerZ & ~0xf;
        int z2 = upperZ & ~0xf;
        for (int x = x1; x <= x2; x += 16) {
            for (int z = z1; z <= z2; z += 16) {
                result.put(x >> 4, z >> 4);
            }
        }
        return result;
    }

    public boolean contains(Location location) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        return contains(x, y, z);
    }

    public boolean contains(RegionCube region) {
        return contains(region.lowerX, region.lowerY, region.lowerY) || contains(region.upperX, region.upperY, region.upperZ);
    }

    public boolean contains(int x, int y, int z) {
        return x >= lowerX && x <= upperX && y >= lowerY && y <= upperY && z >= lowerZ && z <= upperZ;

    }

    public boolean contains(Flag flag) {
        return flags.contains(flag);
    }

    public boolean contains(UUID uuid) {
        return owners.contains(uuid) || members.contains(uuid);
    }

    @Override
    public String toString() {
        return "[" + lowerX + "," + lowerY + "," + lowerZ + "], " +
               "[" + upperX + "," + upperY + "," + upperZ + "]";
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RegionCube && name.equals(((RegionCube) o).getName());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
