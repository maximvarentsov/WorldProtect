package ru.gtncraft.worldprotect;

import com.google.common.base.Joiner;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.mongodb.connection.ServerAddress;
import ru.gtncraft.worldprotect.database.Types;
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
    static Config instance;

    public Config(final FileConfiguration config) {

        super();

        this.addDefaults(config.getRoot());

        this.preventCommands = this.getStringList("region.prevent.commands").stream().map(String::toLowerCase).collect(Collectors.toList());

        this.preventUse = new ArrayList<>();
        for (final String value : this.getStringList("region.prevent.use")) {
            final Material material = Material.matchMaterial(value.toUpperCase());
            if (material != null) {
                this.preventUse.add(material);
            }
        }

        final String material = this.getString("region.tool");
        final Material tool = Material.matchMaterial(material.toUpperCase());
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
            } catch (IllegalArgumentException ex) {

            }
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
        final Collection<String> messages = new ArrayList<>();
        messages.add(ChatColor.YELLOW + getMessage(Messages.region_name) + ": " + ChatColor.WHITE + region.getName());
        messages.add(ChatColor.YELLOW + getMessage(Messages.region_size) + ": " + ChatColor.WHITE + region.getSize());
        messages.add(ChatColor.YELLOW + getMessage(Messages.region_owners) + ": " + ChatColor.WHITE + playerList(region.players(Role.owner)));
        messages.add(ChatColor.YELLOW + getMessage(Messages.region_members) + ": " + ChatColor.WHITE + playerList(region.players(Role.member)));
        final Collection<String> flags = new ArrayList<>();
        for (final Map.Entry<String, Boolean> entry : region.flags().entrySet()) {
            final String value = entry.getValue() ? ChatColor.RED + getMessage(Messages.flag_true) : ChatColor.GRAY + getMessage(Messages.flag_false);
            flags.add(ChatColor.WHITE + entry.getKey() + ": " + value + ChatColor.WHITE);
        }
        messages.add(ChatColor.YELLOW + getMessage(Messages.region_flags) + ": " + Joiner.on(", ").join(flags));
        return messages.toArray(new String[messages.size()]);
    }

    String playerList(Collection<UUID> uuids) {
        List<String> names = new LinkedList<>();
        for (UUID uuid : uuids) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            // TODO remove from region?
            if (player == null) {
                continue;
            }
            names.add(player.getName());
        }
        return Joiner.on(",").join(names);
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
}
