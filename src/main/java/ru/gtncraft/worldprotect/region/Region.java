package ru.gtncraft.worldprotect.region;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import ru.gtncraft.worldprotect.Entity;
import ru.gtncraft.worldprotect.flags.Prevent;

import java.util.*;
import java.util.stream.Collectors;

public class Region extends Entity {

    final Flags flags;
    final Map<UUID, Role> players = new LinkedHashMap<>();
    final Cuboid cuboid;

    public Region(final Map<String, Object> map, final World world) {
        super(map);

        flags = new Flags(asEntity("flags"));

        try {
            this.<UUID>asCollection("owners").forEach(v -> players.put(v, Role.owner));
            this.<UUID>asCollection("members").forEach(v -> players.put(v, Role.member));
        } catch (Throwable ex) {
            this.<String>asCollection("owners")
                .stream()
                .map(v -> Bukkit.getOfflinePlayer(v).getUniqueId())
                .forEach(v -> players.put(v, Role.owner));
            this.<String>asCollection("members")
                    .stream()
                    .map(v -> Bukkit.getOfflinePlayer(v).getUniqueId())
                    .forEach(v -> players.put(v, Role.member));
        }

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
    public Flags flags() {
        return flags;
    }
    /**
     * Get region flag state.
     * @param flag region flag.
     */
    public boolean flag(final Prevent flag) {
        return flags.get(flag);
    }
    /**
     * Get region players owners/members.
     * @param roles player role.
     */
    public Collection<UUID> players(final Role...roles) {

        if (roles.length == 0) {
            return players.keySet();
        }

        Collection<UUID> result = new TreeSet<>();
        Collection<Role> values = Arrays.asList(roles);

        players.entrySet().forEach(e -> {
            if (values.contains(e.getValue())) {
                result.add(e.getKey());
            }
        });

        return result;
    }
    /**
     * Check player with role exists in region.
     *
     * @param player region player.
     * @param roles Player roles.
     */
    public boolean player(final UUID player, final Role...roles) {
        if (players.containsKey(player)) {
            if (roles.length == 0) {
                return true;
            }
            return Arrays.asList(roles).contains(players.get(player));
        }
        return false;
    }

    /**
     * Check location contains region.
     * @param location Location.
     */
    public boolean contains(final Location location) {
        return cuboid.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
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
    public boolean playerDelete(final UUID player, final Role role) {
        return players.remove(player, role);
    }
    /**
     * Add owner/member to region.
     */
    public boolean playerAdd(final UUID player, final Role role) {
        if (players.containsKey(player)) {
            return false;
        }
        players.put(player, role);
        return true;
    }

    public Region update() {
        put("p1", cuboid.asEntity("p1"));
        put("p2", cuboid.asEntity("p2"));
        put("flags", flags);
        put("members", players.entrySet().stream().filter(e -> e.getValue() == Role.member).map(Entry::getKey).collect(Collectors.toList()));
        put("owners", players.entrySet().stream().filter(e -> e.getValue() == Role.owner).map(Entry::getKey).collect(Collectors.toList()));
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
