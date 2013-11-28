package ru.gtncraft.worldprotect.Region;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

final public class Region extends BasicDBObject {

    final private Point p1;
    final private Point p2;
    final private Flags flags;
    final private Players owners;
    final private Players members;

    public Region(final Map map) {
        this.putAll(map);
        this.p1 = new Point(((DBObject) this.get("p1")).toMap());
        this.p2 = new Point(((DBObject) this.get("p2")).toMap());
        this.flags = new Flags(((DBObject) this.get("flags")).toMap());
        this.owners  = new Players(((DBObject) this.get("owners")).toMap());
        this.members = new Players(((DBObject) this.get("members")).toMap());
    }

    public Region(final Location p1, final Location p2) {
        this.p1 = new Point(p1);
        this.p2 = new Point(p2);
        this.flags = new Flags();
        this.owners = new Players();
        this.members = new Players();
        this.update();
    }

    public Map<String, Boolean> getFlags() {
        Map<String, Boolean> values = new HashMap<>();
        for (Flags.prevent flag : Flags.prevent.values()) {
            values.put(flag.name(), flags.get(flag));
        }
        return values;
    }

    public void setName(String value) {
        put("name", value.toLowerCase());
    }

    public String getName() {
        return getString("name");
    }

    public boolean has(final Player player, final Players.role role) {
        String name = player.getName().toLowerCase();
        switch (role) {
            case owner:
                return owners.contains(name);
            case member:
                return members.contains(name);
            case guest:
                return ! contains(name);
        }
        return false;
    }

    public boolean has(final Flags.prevent flag) {
        return flags.get(flag);
    }

    public void set(Flags.prevent flag, boolean value) {
        flags.set(flag, value);
    }

    public Players get(Players.role role) {
        switch (role) {
            case member:
                return members;
            case owner:
                return owners;
        }
        return new Players();
    }

    public boolean remove(String player, Players.role role) {
        player = player.toLowerCase();
        switch (role) {
            case owner:
                return owners.remove(player);
            case member:
                return members.remove(player);
        }
        return false;
    }

    public boolean contains(String player, Players.role role) {
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

    public boolean contains(String player) {
        player = player.toLowerCase();
        return members.contains(player) || owners.contains(player);
    }

    public boolean add(String player, Players.role role) {
        player = player.toLowerCase();
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

    public boolean contains(final Location location) {
        int lx = location.getBlockX();
        int ly = location.getBlockY();
        int lz = location.getBlockZ();
        return ! ( (lx < p1.getX() || lx > p2.getX()) || (ly < p1.getY() || ly > p2.getY()) || (lz < p1.getZ() || lz > p2.getZ()) );
    }

    public void update() {
        this.put("p1", this.p1);
        this.put("p2", this.p2);
        this.put("flags", this.flags);
        this.put("owners", this.owners);
        this.put("members", this.members);
    }

    @Override
    public String toString() {
        return "( p1: " + p1 + " p2: " + p2 + " )";
    }
}
