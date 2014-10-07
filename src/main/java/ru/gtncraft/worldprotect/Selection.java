package ru.gtncraft.worldprotect;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Selection {
    private static WorldEditPlugin we;

    static {
        we = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
    }

    public static List<Location> get(Player player) {
        List<Location> result = new ArrayList<>();
        if (we == null) {

        } else {
            result.add(we.getSelection(player).getMinimumPoint());
            result.add(we.getSelection(player).getMaximumPoint());
        }
        return result;
    }

}
