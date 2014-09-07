package ru.gtncraft.worldprotect.util;

import com.google.common.base.Joiner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import ru.gtncraft.worldprotect.Message;
import ru.gtncraft.worldprotect.Messages;
import ru.gtncraft.worldprotect.region.RegionCube;
import ru.gtncraft.worldprotect.region.Flag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

public class Region {

    public static String[] showInfo(final RegionCube region) {
        Collection<String> messages = new ArrayList<>();

        messages.add(ChatColor.YELLOW + Messages.get(Message.region_name) + ": " + ChatColor.WHITE + region.getName());
        messages.add(ChatColor.YELLOW + Messages.get(Message.region_size) + ": " + ChatColor.WHITE + region);
        messages.add(ChatColor.YELLOW + Messages.get(Message.region_owners) + ": " + ChatColor.WHITE + Joiner.on(",").join(names(region.getOwners())));
        messages.add(ChatColor.YELLOW + Messages.get(Message.region_members) + ": " + ChatColor.WHITE + Joiner.on(",").join(names(region.getMembers())));
        messages.add(showFlags(region.getFlags()));

        return messages.toArray(new String[messages.size()]);
    }

    public static String showFlags(final Collection<Flag> flags) {
        Collection<String> result = new ArrayList<>();
        for (Flag flag : Flag.values()) {
            String value = flags.contains(flag) ?
                           ChatColor.RED + Messages.get(Message.flag_true) :
                           ChatColor.GRAY + Messages.get(Message.flag_false);
            result.add(ChatColor.WHITE + flag.name() + ": " + value + ChatColor.WHITE);
        }
        return ChatColor.YELLOW + Messages.get(Message.flags) + ": " + Joiner.on(", ").join(result);
    }

    public static Collection<String> names(Collection<UUID> uuids) {
        Collection<String> result = new LinkedList<>();
        for (UUID uuid : uuids) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            if (player.hasPlayedBefore()) {
                result.add(player.getName());
            }
        }
        return result;
    }

}
