package ru.gtncraft.worldprotect;

import com.google.common.base.Joiner;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.mongodb.connection.ServerAddress;
import ru.gtncraft.worldprotect.region.Flags;
import ru.gtncraft.worldprotect.storage.Types;
import ru.gtncraft.worldprotect.flags.Prevent;
import ru.gtncraft.worldprotect.region.Region;
import ru.gtncraft.worldprotect.region.Role;

import java.util.*;
import java.util.stream.Collectors;

public class Config extends YamlConfiguration {

    final Collection<String> preventCommands;
    final Collection<Material> preventUse;
    final Material tool;
    final Collection<String> worlds;
    final Collection<Prevent> allowedFlags;
    final Map<String, Object> defaultWorldFlags;
    final Map<String, Object> defaultRegionFlags;
    static Config instance;

    public Config(final FileConfiguration config) {

        super();

        this.addDefaults(config.getRoot());

        this.preventCommands = this.getStringList("region.prevent.commands").stream().map(String::toLowerCase).collect(Collectors.toList());

        this.preventUse = new ArrayList<>();
        for (String value : this.getStringList("region.prevent.use")) {
            Material material = Material.matchMaterial(value.toUpperCase());
            if (material != null) {
                this.preventUse.add(material);
            }
        }

        String material = this.getString("region.tool");
        Material tool = Material.matchMaterial(material.toUpperCase());
        if (tool == null) {
            this.tool = Material.STICK;
        } else {
            this.tool = tool;
        }

        this.worlds = this.getStringList("region.worlds").stream().map(String::toLowerCase).collect(Collectors.toList());

        this.allowedFlags = new ArrayList<>();
        for (final String flag : this.getStringList("region.flags.allowed")) {
            try {
                allowedFlags.add(Prevent.valueOf(flag.toLowerCase()));
            } catch (IllegalArgumentException ignore) {
            }
        }

        defaultWorldFlags = new HashMap<>();
        for (String name : getStringList("world.flags.default")) {
            defaultWorldFlags.put(name, true);
        }

        defaultRegionFlags = new HashMap<>();
        for (String name : getStringList("region.flags.default")) {
            defaultRegionFlags.put(name, true);
        }

        instance = this;
    }

    public static Config getInstance() {
        return instance;
    }

    public List<ServerAddress> getReplicaSet() {
        return getStringList("storage.hosts").stream().map(ServerAddress::new).collect(Collectors.toList());
    }

    public Material getInfoTool() {
        return tool;
    }

    public Collection<String> getPreventCommands() {
        return preventCommands;
    }

    public Collection<Material> getPreventUse() {
        return preventUse;
    }

    public boolean isAllowedFlag(Prevent flag) {
        return allowedFlags.contains(flag);
    }

    public String getMessage(final Messages message) {
        return ChatColor.translateAlternateColorCodes('&', this.getString("messages." + message.name()));
    }

    public String getMessage(final Messages message, Object...args) {
        return String.format(getMessage(message), args);
    }

    public String[] getMessage(final Region region) {
        Collection<String> messages = new ArrayList<>();
        messages.add(ChatColor.YELLOW + getMessage(Messages.region_name) + ": " + ChatColor.WHITE + region.getName());
        messages.add(ChatColor.YELLOW + getMessage(Messages.region_size) + ": " + ChatColor.WHITE + region.getSize());
        messages.add(ChatColor.YELLOW + getMessage(Messages.region_owners) + ": " + ChatColor.WHITE + playerList(region.players(Role.owner)));
        messages.add(ChatColor.YELLOW + getMessage(Messages.region_members) + ": " + ChatColor.WHITE + playerList(region.players(Role.member)));
        messages.add(getFlags(region.flags()));
        return messages.toArray(new String[messages.size()]);
    }

    public String getFlags(final Flags flags) {
        Collection<String> values = new ArrayList<>();
        for (Prevent flag : Prevent.values()) {
            String value = flags.get(flag) ?
                           ChatColor.RED + getMessage(Messages.flag_true) :
                           ChatColor.GRAY + getMessage(Messages.flag_false);
            values.add(ChatColor.WHITE + flag.name() + ": " + value + ChatColor.WHITE);
        }
        return ChatColor.YELLOW + getMessage(Messages.flags) + ": " + Joiner.on(", ").join(values);
    }

    String playerList(Collection<String> players) {
        return Joiner.on(",").join(players);
    }

    public boolean useRegions(final World world) {
        return worlds.contains(world.getName().toLowerCase());
    }

    public Types getStorage() {
        try {
            return Types.valueOf(getString("storage.type").toLowerCase());
        } catch (IllegalArgumentException ex) {
            return Types.file;
        }
    }

    public Map<String, Object> getWorldFlags() {
        return defaultWorldFlags;
    }

    public Map<String, Object> getRegionFlags() {
        return defaultRegionFlags;
    }
}
