package ru.gtncraft.worldprotect.region;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.gtncraft.worldprotect.Entity;
import ru.gtncraft.worldprotect.Roles;
import ru.gtncraft.worldprotect.flags.Prevent;

import java.util.*;

public class Region extends Entity {

    final Flags flags;
    final Collection<UUID> owners;
    final Collection<UUID> members;
    final Cuboid cuboid;

    public Region(final Map map, final World world) {
        super(map);
        flags = new Flags(asEntity("flags"));

        owners = asCollection("owners");
        members = asCollection("members");

        Entity p1 = asEntity("p1");
        Entity p2 = asEntity("p2");
        cuboid = new Cuboid(
            new Location(world, p1.getInteger("x"), p1.getInteger("y"), p1.getInteger("z")),
            new Location(world, p2.getInteger("x"), p2.getInteger("y"), p2.getInteger("z"))
        );
    }

    public Region(final Location point1, final Location point2) {
        super(ImmutableMap.of());
        flags = new Flags(ImmutableMap.of(
                Prevent.build.name(), true,
                Prevent.use.name(), true,
                Prevent.pvp.name(), true,
                Prevent.piston.name(), true
        ));

        owners = new LinkedList<>();
        members = new LinkedList<>();

        cuboid = new Cuboid(point1, point2);
        update();
    }
    /**
     * Get region name.
     */
    public String getName() {
        return getString("name");
    }
    /**
     * Return region flags with state.
     */
    public Map<String, Boolean> get() {
        Map<String, Boolean> values = new HashMap<>();
        for (Prevent flag : Prevent.values()) {
            values.put(flag.name(), flags.get(flag));
        }
        return values;
    }
    /**
     * Get region flag state.
     * @param flag region flag.
     */
    public boolean get(final Prevent flag) {
        return flags.get(flag);
    }
    /**
     * Get region players owners/members.
     * @param role player role.
     */
    public Collection<UUID> get(final Roles role) {
        switch (role) {
            case member:
                return members;
            case owner:
                return owners;
        }
        return ImmutableList.of();
    }
    /**
     * Check player with role exists in region.
     * @param player region player.
     * @param role Player role owner/member.
     */
    public boolean contains(final Player player, final Roles role) {
        return contains(player.getUniqueId(), role);
    }
    /**
     * Check player with role exists in region.
     * @param player region player.
     * @param role Player role owner/member.
     */
    private boolean contains(final UUID player, final Roles role) {
        switch (role) {
            case owner:
                return owners.contains(player);
            case member:
                return members.contains(player);
            case guest:
                return ! contains(player);
        }
        return false;
    }
    /**
     * Check player exists in region.
     */
    public boolean contains(final UUID player) {
        return members.contains(player) || owners.contains(player);
    }
    /**
     * Check location contains region.
     * @param location Location.
     */
    public boolean contains(final Location location) {
        return cuboid.contains(location);
    }
    /**
     * Set region name.
     * @param value region name.
     */
    public void setName(final String value) {
        put("name", value.toLowerCase());
    }
    /**
     * Set region flag.
     * @param flag Flag.
     * @param value Flag value.
     */
    public void set(final Prevent flag, final boolean value) {
        flags.set(flag, value);
    }
    /**
     * Remove player with role.
     * @param player Player name.
     * @param role Player region role Owner/Member.
     */
    public boolean remove(final UUID player, final Roles role) {
        switch (role) {
            case owner:
                return owners.remove(player);
            case member:
                return members.remove(player);
        }
        return false;
    }
    /**
     * Add owner/member to region.
     */
    public boolean add(final UUID player, final Roles role) {
        if (contains(player, role)) {
            return false;
        }
        switch (role) {
            case owner:
                return owners.add(player);
            case member:
                return members.add(player);
            default:
                return false;
        }
    }

    public Region update() {
        put("p1", cuboid.asEntity("p1"));
        put("p2", cuboid.asEntity("p2"));
        put("flags", flags);
        put("members", members);
        put("owners", owners);
        return this;
    }

    public int volume() {
        return cuboid.getVolume();
    }

    public String getSize() {
        return cuboid.toString();
    }

    public Cuboid getCuboid() {
        return cuboid;
    }

    @Override
    public String toString() {
        return getName();
    }
}
