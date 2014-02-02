package ru.gtncraft.worldprotect.Region;

import com.google.common.collect.ImmutableList;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.gtncraft.worldprotect.Roles;
import ru.gtncraft.worldprotect.flags.Prevent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Region extends BasicDBObject {

    private final Point p1;
    private final Point p2;
    private final Flags flags;
    private final BasicDBList owners;
    private final BasicDBList members;

    public Region(final Map map) {
        this.putAll(map);
        this.p1 = new Point(((DBObject) this.get("p1")).toMap());
        this.p2 = new Point(((DBObject) this.get("p2")).toMap());
        this.flags = new Flags(((DBObject) this.get("flags")).toMap());
        this.owners  = (BasicDBList) this.get("owners");
        this.members = (BasicDBList) this.get("members");
    }

    public Region(final Location p1, final Location p2) {
        this.p1 = new Point(p1);
        this.p2 = new Point(p2);
        this.flags = new Flags();
        this.owners = new BasicDBList();
        this.members = new BasicDBList();
        this.update();
    }
    /**
     * Get region name.
     */
    public String getId() {
        return getString("_id");
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
     * @param flag Region flag.
     */
    public boolean get(final Prevent flag) {
        return flags.get(flag);
    }
    /**
     * Get region players owners/members.
     * @param role player role.
     */
    public List<String> get(final Roles role) {
        switch (role) {
            case member:
                members.toArray();
                return ImmutableList.copyOf(members.toArray(new String[0]));
            case owner:
                return ImmutableList.copyOf(owners.toArray(new String[0]));
        }
        return ImmutableList.of();
    }
    /**
     * Check player with role exists in region.
     * @param player Region player.
     * @param role Player role owner/member.
     */
    public boolean contains(final Player player, final Roles role) {
        return contains(player.getName(), role);
    }
    /**
     * Check player with role exists in region.
     * @param player Region player.
     * @param role Player role owner/member.
     */
    private boolean contains(final String player, final Roles role) {
        switch (role) {
            case owner:
                return owners.contains(player.toLowerCase());
            case member:
                return members.contains(player.toLowerCase());
            case guest:
                return ! contains(player.toLowerCase());
        }
        return false;
    }
    /**
     * Check player exists in region.
     */
    public boolean contains(final String player) {
        return members.contains(player.toLowerCase()) || owners.contains(player.toLowerCase());
    }
    /**
     * Check location contains region.
     * @param location Location.
     */
    public boolean contains(final Location location) {
        int lx = location.getBlockX();
        int ly = location.getBlockY();
        int lz = location.getBlockZ();
        return ! ( (lx < p1.getX() || lx > p2.getX()) || (ly < p1.getY() || ly > p2.getY()) || (lz < p1.getZ() || lz > p2.getZ()) );
    }
    /**
     * Set region name.
     * @param value Region name.
     */
    public void setId(final String value) {
        put("_id", new ObjectId(value.toLowerCase()));
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
    public boolean remove(final String player, final Roles role) {
        switch (role) {
            case owner:
                return owners.remove(player.toLowerCase());
            case member:
                return members.remove(player.toLowerCase());
        }
        return false;
    }
    /**
     * Add owner/member to region.
     */
    public boolean add(final String player, final Roles role) {
        if (contains(player, role)) {
            return false;
        }
        switch (role) {
            case owner:
                return owners.add(player.toLowerCase());
            case member:
                return members.add(player.toLowerCase());
            default:
                return false;
        }
    }

    public void update() {
        this.put("p1", this.p1);
        this.put("p2", this.p2);
        this.put("flags", this.flags);
        this.put("members", this.members);
        this.put("owners", this.owners);
    }

    public String getSize() {
        return "( p1: " + p1 + " p2: " + p2 + " )";
    }
}
