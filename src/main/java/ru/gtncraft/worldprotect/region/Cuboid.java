package ru.gtncraft.worldprotect.region;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import ru.gtncraft.worldprotect.Entity;

public class Cuboid extends Entity {
    protected final int x1, y1, z1;
    protected final int x2, y2, z2;
    protected final String worldName;
    /**
     * Construct a Cuboid given two Location objects which represent any two corners of the Cuboid.
     * Note: The 2 locations must be on the same world.
     *
     * @param l1 - One of the corners
     * @param l2 - The other corner
     */
    public Cuboid(final Location l1, final Location l2) {
        super(ImmutableMap.of(
            "p1", new Entity(ImmutableMap.of(
                "x", Math.min(l1.getBlockX(), l2.getBlockX()),
                "y", Math.min(l1.getBlockY(), l2.getBlockY()),
                "z", Math.min(l1.getBlockZ(), l2.getBlockZ())
            )),
            "p2", new Entity(ImmutableMap.of(
                "x", Math.max(l1.getBlockX(), l2.getBlockX()),
                "y", Math.max(l1.getBlockY(), l2.getBlockY()),
                "z", Math.max(l1.getBlockZ(), l2.getBlockZ())
            )
        )));

        Entity p1 = asEntity("p1");
        x1 = p1.getInteger("x");
        y1 = p1.getInteger("y");
        z1 = p1.getInteger("z");

        Entity p2 = asEntity("p2");
        x2 = p2.getInteger("x");
        y2 = p2.getInteger("y");
        z2 = p2.getInteger("z");

        this.worldName = l1.getWorld().getName();
    }
    /**
     * Get the Location of the lower northeast corner of the Cuboid (minimum XYZ co-ordinates).
     *
     * @return Location of the lower northeast corner
     */
    public Location getLowerNE() {
        return new Location(this.getWorld(), this.x1, this.y1, this.z1);
    }
    /**
     * Get the Location of the upper southwest corner of the Cuboid (maximum XYZ co-ordinates).
     *
     * @return Location of the upper southwest corner
     */
    public Location getUpperSW() {
        return new Location(this.getWorld(), this.x2, this.y2, this.z2);
    }
    /**
     * Get the the centre of the Cuboid.
     *
     * @return Location at the centre of the Cuboid
     */
    public Location getCenter() {
        final int x1 = this.getUpperX() + 1;
        final int y1 = this.getUpperY() + 1;
        final int z1 = this.getUpperZ() + 1;
        return new Location(this.getWorld(), this.getLowerX() + (x1 - this.getLowerX()) / 2.0, this.getLowerY() + (y1 - this.getLowerY()) / 2.0, this.getLowerZ() + (z1 - this.getLowerZ()) / 2.0);
    }
    /**
     * Get the Cuboid's world.
     *
     * @return The World object representing this Cuboid's world
     * @throws IllegalStateException if the world is not loaded
     */
    public World getWorld() {
        World world = Bukkit.getWorld(this.worldName);
        if (world == null) {
            throw new IllegalStateException("World '" + this.worldName + "' is not loaded");
        }
        return world;
    }
    /**
     * Get the size of this Cuboid along the X axis
     *
     * @return	Size of Cuboid along the X axis
     */
    public int getSizeX() {
        return (this.x2 - this.x1) + 1;
    }
    /**
     * Get the size of this Cuboid along the Y axis
     *
     * @return	Size of Cuboid along the Y axis
     */
    public int getSizeY() {
        return (this.y2 - this.y1) + 1;
    }
    /**
     * Get the size of this Cuboid along the Z axis
     *
     * @return	Size of Cuboid along the Z axis
     */
    public int getSizeZ() {
        return (this.z2 - this.z1) + 1;
    }
    /**
     * Get the minimum X co-ordinate of this Cuboid
     *
     * @return	the minimum X co-ordinate
     */
    public int getLowerX() {
        return this.x1;
    }
    /**
     * Get the minimum Y co-ordinate of this Cuboid
     *
     * @return	the minimum Y co-ordinate
     */
    public int getLowerY() {
        return this.y1;
    }
    /**
     * Get the minimum Z co-ordinate of this Cuboid
     *
     * @return	the minimum Z co-ordinate
     */
    public int getLowerZ() {
        return this.z1;
    }
    /**
     * Get the maximum X co-ordinate of this Cuboid
     *
     * @return	the maximum X co-ordinate
     */
    public int getUpperX() {
        return this.x2;
    }
    /**
     * Get the maximum Y co-ordinate of this Cuboid
     *
     * @return	the maximum Y co-ordinate
     */
    public int getUpperY() {
        return this.y2;
    }
    /**
     * Get the maximum Z co-ordinate of this Cuboid
     *
     * @return	the maximum Z co-ordinate
     */
    public int getUpperZ() {
        return this.z2;
    }
    /**
     * Return true if the point at (x,y,z) is contained within this Cuboid.
     *
     * @param x	- The X co-ordinate
     * @param y	- The Y co-ordinate
     * @param z	- The Z co-ordinate
     * @return true if the given point is within this Cuboid, false otherwise
     */
    public boolean contains(final int x, final int y, final int z) {
        return x >= this.x1 && x <= this.x2 && y >= this.y1 && y <= this.y2 && z >= this.z1 && z <= this.z2;
    }
    /**
     * Check if the given Location is contained within this Cuboid.
     *
     * @param location	- The Location to check for
     * @return true if the Location is within this Cuboid, false otherwise
     */
    public boolean contains(final Location location) {
        return this.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
    /**
     * Get the volume of this Cuboid.
     *
     * @return The Cuboid volume, in blocks
     */
    public int getVolume() {
        return this.getSizeX() * this.getSizeY() * this.getSizeZ();
    }

    public Multimap<Integer, Integer> getChunks() {
        Multimap<Integer, Integer> result = ArrayListMultimap.create();
        int x1 = this.getLowerX() & ~0xf;
        int x2 = this.getUpperX() & ~0xf;
        int z1 = this.getLowerZ() & ~0xf;
        int z2 = this.getUpperZ() & ~0xf;
        for (int x = x1; x <= x2; x += 16) {
            for (int z = z1; z <= z2; z += 16) {
                result.put(x >> 4, z >> 4);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "[" + this.x1 + "," + this.y1 + "," + this.z1 + "], [" + this.x2 + "," + this.y2 + "," + this.z2 + "]";
    }
}