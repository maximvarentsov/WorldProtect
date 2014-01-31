package ru.gtncraft.worldprotect.Region;

import com.google.common.collect.ImmutableList;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.gtncraft.worldprotect.Roles;
import ru.gtncraft.worldprotect.flags.Prevent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Region extends BasicDBObject {

    final private Point p1;
    final private Point p2;
    final private Flags flags;
    final private BasicDBList owners;
    final private BasicDBList members;

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

    public String getId() {
        return getString("_id");
    }

    public Map<String, Boolean> getFlags() {
        Map<String, Boolean> values = new HashMap<>();
        for (Prevent flag : Prevent.values()) {
            values.put(flag.name(), flags.get(flag));
        }
        return values;
    }

    public boolean get(final Prevent flag) {
        return flags.get(flag);
    }

    public List get(final Roles role) {
        switch (role) {
            case member:
                return ImmutableList.copyOf(members);
            case owner:
                return ImmutableList.copyOf(owners);
        }
        return ImmutableList.of();
    }

    public boolean contains(final Player player, final Roles role) {
        return contains(player.getName(), role);
    }

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

    private boolean contains(final String player) {
        return members.contains(player.toLowerCase()) || owners.contains(player.toLowerCase());
    }

    public boolean contains(final Location location) {
        int lx = location.getBlockX();
        int ly = location.getBlockY();
        int lz = location.getBlockZ();
        return ! ( (lx < p1.getX() || lx > p2.getX()) || (ly < p1.getY() || ly > p2.getY()) || (lz < p1.getZ() || lz > p2.getZ()) );
    }

    public void setId(final String value) {
        put("_id", value.toLowerCase());
    }

    public void set(final Prevent flag, final boolean value) {
        flags.set(flag, value);
    }

    public boolean remove(final String player, final Roles role) {
        switch (role) {
            case owner:
                return owners.remove(player.toLowerCase());
            case member:
                return members.remove(player.toLowerCase());
        }
        return false;
    }

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
