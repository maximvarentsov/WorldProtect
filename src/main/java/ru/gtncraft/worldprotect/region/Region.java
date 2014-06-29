package ru.gtncraft.worldprotect.region;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.gtncraft.worldprotect.Config;
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

        this.<String>asCollection("owners").forEach(v -> players.put(UUID.fromString(v), Role.owner));
        this.<String>asCollection("members").forEach(v -> players.put(UUID.fromString(v), Role.member));

        Entity p1 = asEntity("p1");
        Entity p2 = asEntity("p2");

        cuboid = new Cuboid(
            new Location(world, p1.getInteger("x"), p1.getInteger("y"), p1.getInteger("z")),
            new Location(world, p2.getInteger("x"), p2.getInteger("y"), p2.getInteger("z"))
        );
    }

    public Region(final Location point1, final Location point2) {
        super(ImmutableMap.of());

        flags = new Flags(Config.getInstance().getRegionFlags());

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
     * @param values player role.
     */
    public Collection<UUID> players(final Role...values) {

        if (values.length == 0) {
            return players.keySet();
        }

        Collection<UUID> result = new TreeSet<>();
        Collection<Role> roles = Arrays.asList(values);

        players.entrySet().forEach(e -> {
            UUID uuid = e.getKey();
            Role role = e.getValue();
            if (roles.contains(role)) {
                result.add(uuid);
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
    public boolean player(final Player player, final Role...roles) {
        if (players.containsKey(player.getUniqueId())) {
            if (roles.length == 0) {
                return true;
            }
            return Arrays.asList(roles).contains(players.get(player.getUniqueId()));
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
     * @param name Player name.
     * @param role Player region role Owner/Member.
     */
    public boolean playerDelete(final String name, final Role role) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        return players.remove(player.getUniqueId(), role);
    }
    /**
     * Add owner/member to region.
     */
    public boolean playerAdd(final String name, final Role role) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);

        if (players.containsKey(player.getUniqueId())) {
            return false;
        }
        players.put(player.getUniqueId(), role);
        return true;
    }

    public Region update() {
        put("p1", cuboid.asEntity("p1").toDocument());
        put("p2", cuboid.asEntity("p2").toDocument());
        put("flags", flags.toDocument());
        put("members", players.entrySet().stream().filter(e -> e.getValue() == Role.member).map(v -> v.getKey().toString()).collect(Collectors.toList()));
        put("owners", players.entrySet().stream().filter(e -> e.getValue() == Role.owner).map(v -> v.getKey().toString()).collect(Collectors.toList()));
        return this;
    }

    public Role getPlayerRole(final OfflinePlayer player) {
        return players.get(player.getUniqueId());
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
