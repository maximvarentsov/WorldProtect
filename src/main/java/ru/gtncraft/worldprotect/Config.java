package ru.gtncraft.worldprotect;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Config extends YamlConfiguration {

    private final List<String> preventCommands;
    private final List<Material> preventUse;
    private final Material tool;

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
}
