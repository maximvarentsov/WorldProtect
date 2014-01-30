package ru.gtncraft.worldprotect;

import com.google.common.base.Joiner;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.gtncraft.worldprotect.Region.Players;
import ru.gtncraft.worldprotect.Region.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Config extends YamlConfiguration {

    private final List<String> preventCommands;
    private final List<Material> preventUse;
    private final Material tool;
    private final ConfigurationSection messages;

    public Config(final FileConfiguration config) {

        this.preventCommands = new ArrayList<>();
        for (String command : config.getStringList("region.prevent.commands")) {
            this.preventCommands.add(command.toLowerCase());
        }

        this.preventUse = new ArrayList<>();
        for (String value : config.getStringList("region.prevent.use")) {
            Material material = Material.matchMaterial(value.toUpperCase());
            if (material != null) {
                this.preventUse.add(material);
            }
        }

        String material = config.getString("region.item");
        Material tool = Material.matchMaterial(material.toUpperCase());
        if (tool == null) {
            this.tool = Material.STICK;
        } else {
            this.tool = tool;
        }

        this.messages = config.getConfigurationSection("messages");
    }

    public Material getInfoTool() {
        return tool;
    }

    public List<String> getPreventCommands() {
        return preventCommands;
    }

    public List<Material> getPreventUse() {
        return preventUse;
    }

    public String getMessage(final Messages message) {
        return ChatColor.translateAlternateColorCodes('&', messages.getString(message.name()));
    }

    public String getMessage(final Messages message, String...args) {
        return String.format(getMessage(message), args);
    }

    public String[] getMessage(List<Region> regions) {
        List<String> messages = new ArrayList<>();
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
        List<String> messages = new ArrayList<>();

        messages.add(ChatColor.YELLOW + getMessage(Messages.region_name) + ": "    + ChatColor.WHITE + region.getName());
        messages.add(ChatColor.YELLOW + getMessage(Messages.region_size) + ": " + ChatColor.WHITE + region.getSize());
        messages.add(ChatColor.YELLOW + getMessage(Messages.region_owners) + ": " + ChatColor.WHITE + region.get(Players.role.owner));
        messages.add(ChatColor.YELLOW + getMessage(Messages.region_members) + ": " + ChatColor.WHITE + region.get(Players.role.member));
        List<String> flags = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : region.getFlags().entrySet()) {
            String value = entry.getValue() ? ChatColor.RED + getMessage(Messages.flag_true) : ChatColor.GRAY + getMessage(Messages.flag_true);
            flags.add(ChatColor.WHITE + entry.getKey() + ": " + value + ChatColor.WHITE);
        }
        messages.add(ChatColor.YELLOW + getMessage(Messages.region_flags) + ": " + Joiner.on(", ").join(flags));

        return messages.toArray(new String[0]);
    }
}
