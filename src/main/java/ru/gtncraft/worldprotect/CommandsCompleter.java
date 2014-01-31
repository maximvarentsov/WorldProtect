package ru.gtncraft.worldprotect;

import com.google.common.collect.ImmutableList;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import ru.gtncraft.worldprotect.Region.Players;
import ru.gtncraft.worldprotect.Region.Region;
import ru.gtncraft.worldprotect.flags.Prevent;

import java.util.*;

public class CommandsCompleter implements TabCompleter {

    private final WorldProtect plugin;
    private final List<String> subs = ImmutableList.of(
            "define", "delete", "addowner", "deleteowner", "addmemeber", "deletemember",
            "info", "list", "flag", "save"
    );
    private final List<String> bool = ImmutableList.of("true", "false");
    private final List<String> flags = new ArrayList<>();

    public CommandsCompleter(final WorldProtect plugin) {
        this.plugin = plugin;
        for (Prevent flag : Prevent.values()) {
            flags.add(flag.name());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // Remember that we can return null to default to online player name matching
        /**
          define <region>
          delete <region>
          addowner <region> <player>
          deleteowner <region> <player>
          addmember <region> <player>
          deletemember <region> <player>
          info [region]
          list
          flag <region> set <flag> <true|false>
          save
         */
        String lastArg = args[args.length - 1];

        if (args.length <= 1) {
            return partial(args[0], subs);
        } else if (args.length == 2) {
            String sub = args[0];
            if ("delete".equals(sub) || "deleteowner".equals(sub) || "deletemember".equals(sub) ||
                "addowner".equals(sub) || "addmember".equals(sub) || "info".equals(sub) || "flag".equals(sub)) {
                return partial(lastArg, allRegions((Player) sender));
            }
        } else if (args.length == 3) {
            String sub = args[0];
            String region = args[1];
            if ("deleteowner".equals(sub)) {
                return partial(lastArg, allRegionPlayers(((Player) sender).getWorld(), region, Players.role.owner));
            } else if ("deletemember".equals(sub)) {
                return partial(lastArg, allRegionPlayers(((Player) sender).getWorld(), region, Players.role.member));
            } else if ("addowner".equals(sub) || "addmember".equals(sub)) {
                return  null;
            } else if ("flag".equals(sub)) {
                return partial(lastArg, Arrays.asList("set"));
            }
        } else if (args.length == 4) {
            String sub = args[0];
            if ("flag".equals(sub)) {
                return partial(lastArg, flags);
            }
        } else if (args.length == 5) {
            String sub = args[0];
            if ("flag".equals(sub)) {
                return partial(lastArg, bool);
            }
        }

        return ImmutableList.of();
    }

    private List<String> partial(final String token, final Collection<String> from) {
        return StringUtil.copyPartialMatches(token, from, new ArrayList<String>(from.size()));
    }

    private List<String> allRegions(final Player player) {
        List<String> result = new ArrayList<>();
        if (player.hasPermission(Permissions.admin)) {
            for (Map.Entry<String, Region> entry : plugin.getRegionManager().get(player.getWorld()).entrySet()) {
                result.add(entry.getKey());
            }
        } else {
            for (Region region : plugin.getRegionManager().get(player, Players.role.owner)) {
                result.add(region.getName());
            }
        }
        return result;
    }

    private List<String> allRegionPlayers(final World world, final String name, Players.role role) {
        List<String> result = new ArrayList<>();
        Region region = plugin.getRegionManager().get(world, name);
        if (region != null) {
            for (Object player : region.get(role)) {
                result.add((String) player);
            }
        }
        return result;
    }
}