package ru.gtncraft.worldprotect.Region;

import com.mongodb.BasicDBList;
import java.util.Map;

final public class Players extends BasicDBList {

    public static enum role {
        owner, member, guest, admin
    }

    public Players() {}

    public Players(Map map) {
        putAll(map);
    }
}
