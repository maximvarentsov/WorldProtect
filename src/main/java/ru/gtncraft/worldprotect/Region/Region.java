package ru.gtncraft.worldprotect.Region;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.Map;

final public class Region extends BasicDBObject {

    final public Point p1;
    final public Point p2;
    final public Players owners;
    final public Players members;
    final public Flags flags;

    public Region(Map map) {
        this.putAll(map);
        this.p1 = new Point(((DBObject) this.get("p1")).toMap());
        this.p2 = new Point(((DBObject) this.get("p2")).toMap());
        this.owners  = new Players(((DBObject) this.get("owners")).toMap());
        this.members = new Players(((DBObject) this.get("members")).toMap());
        this.flags = new Flags(((DBObject) this.get("flags")).toMap());
    }

    public Region(Location p1, Location p2) {
        this.p1 = new Point(p1);
        this.p2 = new Point(p2);
        this.owners = new Players();
        this.members = new Players();
        this.flags = new Flags();
        this.put("p1", this.p1);
        this.put("p2", this.p2);
        this.put("owners", this.owners);
        this.put("members", this.members);
        this.put("flags", this.flags);
    }

    public void setName(String value) {
        put("name", value);
    }

    public String getName() {
        return getString("name");
    }

    public boolean isOwner(final Player player) {
        return owners.has(player.getName()) || player.hasPermission("worldprotect.admin");
    }

    public boolean isMember(final Player player) {
        return members.has(player.getName()) || player.hasPermission("worldprotect.admin");
    }

    public boolean isPlayer(final Player player) {
        return isOwner(player) || isMember(player);
    }

    public boolean isGuest(final Player player) {
        if (player == null) {
            return true;
        }
        return ! isPlayer(player);
    }

    public boolean contains(Location location) {
        double lx = location.getX();
        double ly = location.getY();
        double lz = location.getZ();
        return ! ( (lx < p1.getX() || lx > p2.getX()) || (ly < p1.getY() || ly > p2.getY()) || (lz < p1.getZ() || lz > p2.getZ()) );
    }
}
