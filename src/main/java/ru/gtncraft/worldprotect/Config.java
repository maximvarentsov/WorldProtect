package ru.gtncraft.worldprotect;

import com.google.common.base.Joiner;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.gtncraft.worldprotect.flags.Prevent;
import ru.gtncraft.worldprotect.region.Region;
import java.util.*;

public class Config extends YamlConfiguration {

    private final Collection<String> preventCommands;
    private final Collection<Material> preventUse;
    private final Material tool;
    private final Collection<String> worlds;
    private final Collection<Prevent> allowedFlags;

    public Config(final FileConfiguration config) {

        super();

        this.addDefaults(config.getRoot());

        this.preventCommands = new ArrayList<>();
        for (String command : this.getStringList("region.prevent.commands")) {
            this.preventCommands.add(command.toLowerCase());
        }

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

        this.worlds = new ArrayList<>();
        for (String world : this.getStringList("region.worlds")) {
            worlds.add(world.toLowerCase());
        }

        this.allowedFlags = new ArrayList<>();
        for (String flag : this.getStringList("region.flags.allowed")) {
            try {
                allowedFlags.add(Prevent.valueOf(flag.toLowerCase()));
            } catch (IllegalArgumentException ex) {

            }
        }
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

    public String[] getMessage(Collection<Region> regions) {
        Collection<String> messages = new ArrayList<>();
        if (regions.size() > 0) {
            for (Region region : regions) {
                messages.addAll(Arrays.asList(getMessage(region)));
            }
        } else {
            messages.add(getMessage(Messages.error_region_not_found));
        }
        return messages.toArray(new String[0]);
    }

    public String[] getMessage(Region region) {
        Collection<String> messages = new ArrayList<>();

        messages.add(ChatColor.YELLOW + getMessage(Messages.region_name) + ": "    + ChatColor.WHITE + region.getName());
        messages.add(ChatColor.YELLOW + getMessage(Messages.region_size) + ": " + ChatColor.WHITE + region.getSize());
        messages.add(ChatColor.YELLOW + getMessage(Messages.region_owners) + ": " + ChatColor.WHITE + region.get(Roles.owner));
        messages.add(ChatColor.YELLOW + getMessage(Messages.region_members) + ": " + ChatColor.WHITE + region.get(Roles.member));
        Collection<String> flags = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : region.get().entrySet()) {
            String value = entry.getValue() ? ChatColor.RED + getMessage(Messages.flag_true) : ChatColor.GRAY + getMessage(Messages.flag_false);
            flags.add(ChatColor.WHITE + entry.getKey() + ": " + value + ChatColor.WHITE);
        }
        messages.add(ChatColor.YELLOW + getMessage(Messages.region_flags) + ": " + Joiner.on(", ").join(flags));

        return messages.toArray(new String[0]);
    }

    public boolean useRegions(final World world) {
        return worlds.contains(world.getName().toLowerCase());
    }
}
