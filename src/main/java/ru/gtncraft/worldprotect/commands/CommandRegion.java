package ru.gtncraft.worldprotect.commands;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import ru.gtncraft.worldprotect.*;
import ru.gtncraft.worldprotect.region.RegionCube;
import ru.gtncraft.worldprotect.region.Flag;
import ru.gtncraft.worldprotect.util.Region;

import java.util.*;

import static ru.gtncraft.worldprotect.util.Strings.partial;

public class CommandRegion implements CommandExecutor, TabCompleter {

    private final ProtectionManager manager;
    private final WorldEditPlugin we;
    private final Collection<String> commands = ImmutableList.of(
        "define", "delete", "addowner", "deleteowner", "addmember", "deletemember", "info", "list", "flag", "help"
    );
    private final long maxRegions;
    private final int maxVolume;
    private final Collection<String> allowedFlags = new ArrayList<>();
    private final Collection<String> worlds = new ArrayList<>();

    public CommandRegion(final WorldProtect plugin) {
        manager = plugin.getProtectionManager();
        we = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        maxRegions = plugin.getConfig().getLong("region.maxPerPlayer");
        maxVolume = plugin.getConfig().getInt("region.maxVolume");
        allowedFlags.addAll(plugin.getConfig().getStringList("region.flag.allowed"));
        worlds.addAll(plugin.getConfig().getStringList("region.worlds"));

        plugin.getCommand("region").setExecutor(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command pcommand, String label, String[] args) {
        /**
         * Remember that we can return null to default to online player name matching.
         */
        if (args.length > 1) {
            final String lastArg = args[args.length - 1];
            final String command = args[0].toLowerCase();
            switch (args.length) {
                case 2:
                    if (commands.contains(command) && !("list".equals(command) || "help".equals(command)) ) {
                        return partial(lastArg, regions((Player) sender));
                    }
                    break;
                case 3:
                    final String region = args[1];
                    switch (command) {
                        case "deleteowner":
                            return partial(lastArg, players(((Player) sender).getWorld(), region, true));
                        case "deletemember":
                            return partial(lastArg, players(((Player) sender).getWorld(), region, false));
                        case "addowner":
                        case "addmember":
                            return null;
                        case "flag":
                            return partial(lastArg, ImmutableList.of("set"));
                    }
                    break;
                case 4:
                    if ("flag".equals(command)) {
                        return partial(lastArg, Flag.toArray());
                    }
                    break;
                case 5:
                    if ("flag".equals(command)) {
                        return partial(lastArg, ImmutableList.of("true", "false"));
                    }
                    break;
            }
        } else if (args.length <= 1) {
            return partial(args[0], commands);
        }
        return ImmutableList.of();
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel, final String[] args) {
        boolean usage = false;

        if (!(sender instanceof Player)) {
            sender.sendMessage(Messages.get(Message.error_player_command));
            return true;
        }

        Player player = (Player) sender;

        if (!worlds.contains(player.getWorld().getName())) {
            player.sendMessage(Messages.get(Message.error_regions_disabled, player.getWorld().getName()));
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "define":
                case "claim":
                case "redefine":
                    define(player, args);
                    break;
                case "delete":
                    delete(player, args);
                    break;
                case "addowner":
                    addPlayer(player, args, true);
                    break;
                case "deleteowner":
                    removePlayer(player, args, true);
                    break;
                case "addmember":
                    addPlayer(player, args, false);
                    break;
                case "deletemember":
                    removePlayer(player, args, false);
                    break;
                case "info":
                    info(player, args);
                    break;
                case "list":
                    list(player);
                    break;
                case "flag":
                    switch (args[2].toLowerCase()) {
                        case "set":
                            flag(player, args);
                            break;
                    }
                    break;
                case "help":
                    usage = true;
                    break;
                default:
                    player.sendMessage(Messages.get(Message.error_unknown_command, commandLabel));
                    break;
            }
        } catch (CommandException ex) {
            player.sendMessage(ex.getMessage());
        } catch (ArrayIndexOutOfBoundsException ignore) {
            player.sendMessage(Messages.get(Message.error_unknown_command, commandLabel));
        }
        return !usage;
    }

    private void define(final Player sender, final String[] args) throws CommandException {
        if (we == null) {
            throw new CommandException(Messages.get(Message.error_worldedit_not_found));
        }

        Selection selection = we.getSelection(sender);
        if (selection == null) {
            throw new CommandException(Messages.get(Message.error_region_selection));
        }

        Location p1 = selection.getMinimumPoint();
        Location p2 = selection.getMaximumPoint();

        if (args.length < 2) {
            throw new CommandException(Messages.get(Message.error_input_region_name));
        }

        String name = args[1];

        if (manager.get(sender.getWorld(), name) != null) {
            throw new CommandException(Messages.get(Message.error_region_name_exists, name));
        }

        RegionCube region = new RegionCube(p1, p2);
        region.setName(name);
        region.addOwner(sender.getUniqueId());

        if (!sender.hasPermission(Permission.admin)) {
            /**
             *  Check region have overlay with another.
             */
            Collection<RegionCube> regions = manager.get(sender.getWorld(), region);
            for (RegionCube overlay : regions) {
                if (overlay.contains(region) || region.contains(overlay)) {
                    if (!overlay.getOwners().contains(sender.getUniqueId())) {
                        throw new CommandException(Messages.get(Message.error_region_overlay));
                    }
                }
            }

        }

        if (!sender.hasPermission(Permission.admin) || !sender.hasPermission(Permission.unlimited)) {
            /**
             * Check region creation per player.
             */
            long total = manager.getOwn(sender).size();
            if (maxRegions > 0 && total >= maxRegions) {
                throw new CommandException( Messages.get(Message.error_region_created_max, maxRegions));
            }
            /**
             * Check region volume
             */
            if (region.volume() > maxVolume) {
                throw new CommandException(Messages.get(Message.error_region_max_volume, maxVolume, region.volume()));
            }
        }

        manager.add(sender.getWorld(), region);
        sender.sendMessage(Messages.get(Message.success_region_created, name));
    }

    private void list(final Player player) {
        player.sendMessage(Messages.get(Message.region_own_list) + ":");
        for (RegionCube region : manager.getOwn(player)) {
            player.sendMessage(Messages.get(Message.region_name) + ": " + region.getName() + " " + region);
        }
    }

    private void info(final Player sender, final String[] args) throws CommandException {
        if (args.length > 1) {
            String name = args[1];
            RegionCube region = manager.get(sender.getWorld(), name);
            if (region == null) {
                throw new CommandException(Messages.get(Message.error_input_region_not_found, name));
            }
            sender.sendMessage(Region.showInfo(region));
        } else {
            Collection<RegionCube> values = manager.get(sender.getLocation());
            if (values.size() > 0) {
                for (RegionCube region : values) {
                    sender.sendMessage(Region.showInfo(region));
                }
            } else {
                sender.sendMessage(Messages.get(Message.error_region_not_found));
            }
        }
    }

    private void delete(final Player player, final String[] args) throws CommandException {

        if (args.length < 2) {
            throw new CommandException(Messages.get(Message.error_input_region_name));
        }

        String name = args[1];

        RegionCube region = manager.get(player.getWorld(), name);

        if (region == null) {
            throw new CommandException(Messages.get(Message.error_input_region_not_found, name));
        }

        testPermission(player, region);

        manager.delete(player.getWorld(), name);
        player.sendMessage(Messages.get(Message.success_region_deleted, name));
    }

    private void flag(final Player player, final String[] args) throws CommandException {

        if (args.length < 2) {
            throw new CommandException(Messages.get(Message.error_input_region_name));
        }

        if (args.length < 4) {
            throw new CommandException(Messages.get(Message.error_input_flag));
        }

        if (args.length < 5) {
            throw new CommandException(Messages.get(Message.error_input_flag_value));
        }

        String name = args[1];
        String flag = args[3];
        String value = args[4];

        RegionCube region = manager.get(player.getWorld(), name);

        if (region == null) {
            throw new CommandException(Messages.get(Message.error_input_region_not_found, name));
        }

        boolean valueFlag;
        // reverse values, prevent true
        switch (value.toLowerCase()) {
            case "true":
                valueFlag = false;
                break;
            case "false":
                valueFlag = true;
                break;
            default:
                throw new CommandException(Messages.get(Message.error_input_flag_invalid_value));
        }

        Flag prevent;
        try {
            prevent = Flag.valueOf(flag);
        } catch (IllegalArgumentException ex) {
            throw new CommandException(Messages.get(Message.error_input_flag_unknown, flag));
        }

        boolean perms = false;

        if (player.hasPermission(Permission.admin)) {
            perms = true;
        } else if (region.getOwners().contains(player.getUniqueId())) {
            if (allowedFlags.contains(prevent.name())) {
                perms = true;
            }
        }

        if (perms) {
            if (valueFlag) {
                region.addFlag(prevent);
            } else {
                region.removeFlag(prevent);
            }
            String flagState = valueFlag ? Messages.get(Message.flag_true) : Messages.get(Message.flag_false);
            player.sendMessage(Messages.get(Message.success_region_flag_set, flag, name, flagState));
        } else {
            throw new CommandException(Messages.get(Message.error_no_permission));
        }
    }

    private void addPlayer(final Player sender, final String[] args, boolean role) throws CommandException {

        if (args.length < 2) {
            throw new CommandException(Messages.get(Message.error_input_region_name));
        }

        String name = args[1];

        RegionCube region = manager.get(sender.getWorld(), name);

        if (region == null) {
            throw new CommandException(Messages.get(Message.error_input_region_not_found, name));
        }

        String playerName = args[2];

        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);

        if (!player.hasPlayedBefore()) {
            throw new CommandException(Messages.get(Message.error_player_not_found, playerName));
        }

        testPermission(sender, region);

        boolean result;
        if (role) {
            result = region.addOwner(player.getUniqueId());
        } else {
            result = region.addMember(player.getUniqueId());
        }

        if (!result) {
            String playerRole = role ? Messages.get(Message.role_owner) : Messages.get(Message.role_member);
            throw new CommandException(Messages.get(Message.error_region_contains_player, player.getName(), name, playerRole));
        }
        sender.sendMessage(Messages.get(Message.success_region_player_add, player.getName(), name));
    }

    private void removePlayer(final Player sender, final String[] args, final boolean role) throws CommandException {

        if (args.length < 2) {
            throw new CommandException(Messages.get(Message.error_input_region_name));
        }

        if (args.length < 3) {
            throw new CommandException(Messages.get(Message.error_input_player));
        }

        String regionName = args[1];
        String playerName = args[2];

        RegionCube region = manager.get(sender.getWorld(), regionName);

        if (region == null) {
            throw new CommandException(Messages.get(Message.error_input_region_not_found, regionName));
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);

        if (!player.hasPlayedBefore()) {
            throw new CommandException(Messages.get(Message.error_player_not_found, playerName));
        }

        testPermission(sender, region);

        if (role) {
            if (!region.removeOwner(player.getUniqueId())) {
                throw new CommandException(Messages.get(Message.error_region_player_exists, player.getName(), regionName));
            }
        } else {
            if (!region.removeMember(player.getUniqueId())) {
                throw new CommandException(Messages.get(Message.error_region_player_exists, player.getName(), regionName));
            }
        }

        sender.sendMessage(Messages.get(Message.success_region_player_delete, player.getName(), regionName));
    }

    private void testPermission(final Player sender, final RegionCube region) throws CommandException {
        if (!(sender.hasPermission(Permission.admin) || region.getOwners().contains(sender.getUniqueId()))) {
            throw new CommandException(Messages.get(Message.error_no_permission));
        }
    }

    // FIXME: don't iterate every time all regions
    private Collection<String> regions(Player player) {
        Collection<String> result = new ArrayList<>();
        for (RegionCube region : manager.getOwn(player)) {
            if (region.contains(player.getUniqueId())) {
                result.add(player.getName());
            }
        }
        return result;
    }

    private Collection<String> players(final World world, final String name, boolean role) {
        Collection<String> result = new LinkedList<>();
        RegionCube region = manager.get(world, name);
        if (region != null) {
            if (role) {
                for (UUID uuid : region.getOwners()) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                    if (player.hasPlayedBefore()) {
                        result.add(player.getName());
                    }
                }
            } else {
                for (UUID uuid : region.getMembers()) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                    if (player.hasPlayedBefore()) {
                        result.add(player.getName());
                    }
                }
            }
        }
        return result;
    }
}
